package com.engineeringbench.service;

import com.engineeringbench.model.SandboxResult;
import com.engineeringbench.model.SandboxRuntime;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.OutputLogEvent;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service("fargateSandboxService")
public class FargateSandboxService implements SandboxService {

    private static final Region REGION =
            Region.US_EAST_2;

    private static final String CLUSTER =
            "engineering-bench";

    private static final String TASK_DEFINITION =
            "engineering-bench-sandbox:2";

    private static final String CONTAINER_NAME =
            "engineering-bench-sandbox";

    private static final String SUBNET_ID =
            "subnet-0f2014463b95a7c00";

    private static final String SECURITY_GROUP_ID =
            "sg-05db641b523a00737";

    private final EcsClient ecsClient;
    private final CloudWatchLogsClient logsClient;

    public FargateSandboxService() {

        this.ecsClient =
                EcsClient.builder()
                        .region(REGION)
                        .build();

        this.logsClient =
                CloudWatchLogsClient.builder()
                        .region(REGION)
                        .build();
    }

    // ============================================================
    // Existing one-shot execution
    // ============================================================

    @Override
    public SandboxResult execute(
            String repository,
            List<String> commands) {

        return execute(
                repository,
                null,
                commands
        );
    }

    @Override
    public SandboxResult execute(
            String repository,
            String revision,
            List<String> commands) {

        StringBuilder script =
                new StringBuilder();

        script.append("""
                set -e

                echo "Cloning repository..."
                git clone %s /workspace/repository

                echo "Repository cloned successfully"

                cd /workspace/repository

                """.formatted(repository));

        if (revision != null
                && !revision.isBlank()) {

            script.append("""
                    echo "Checking out revision..."
                    git checkout %s
                    echo "Revision checked out successfully"

                    """.formatted(revision));
        }

        script.append("""
                echo "Executing commands..."
                """);

        for (String command : commands) {

            script.append("echo \">>> ")
                    .append(command)
                    .append("\"\n");

            script.append(command)
                    .append("\n");
        }

        script.append("""
                
                echo "All commands completed successfully"
                """);

        return runOneShotTask(script.toString());
    }

    // ============================================================
    // Persistent runtime
    // ============================================================

    @Override
    public SandboxRuntime start(
            String repository,
            String revision) {

        StringBuilder script =
                new StringBuilder();

        script.append("""
                set -e

                echo "Starting persistent Engineering Bench sandbox..."

                mkdir -p /workspace

                echo "Cloning repository..."

                git clone %s /workspace/repository

                echo "Repository cloned successfully"

                cd /workspace/repository

                """.formatted(repository));

        if (revision != null
                && !revision.isBlank()) {

            script.append("""
                    echo "Checking out revision..."

                    git checkout %s

                    echo "Revision checked out successfully"

                    """.formatted(revision));
        }

        script.append("""
                echo "Persistent sandbox ready."

                cd /workspace/repository

                exec sleep infinity
                """);

        RunTaskResponse response =
                ecsClient.runTask(
                        RunTaskRequest.builder()
                                .cluster(CLUSTER)
                                .taskDefinition(
                                        TASK_DEFINITION
                                )
                                .launchType(
                                        LaunchType.FARGATE
                                )
                                .enableExecuteCommand(true)
                                .networkConfiguration(
                                        NetworkConfiguration.builder()
                                                .awsvpcConfiguration(
                                                        AwsVpcConfiguration.builder()
                                                                .subnets(
                                                                        SUBNET_ID
                                                                )
                                                                .securityGroups(
                                                                        SECURITY_GROUP_ID
                                                                )
                                                                .assignPublicIp(
                                                                        AssignPublicIp.ENABLED
                                                                )
                                                                .build()
                                                )
                                                .build()
                                )
                                .overrides(
                                        TaskOverride.builder()
                                                .containerOverrides(
                                                        ContainerOverride.builder()
                                                                .name(
                                                                        CONTAINER_NAME
                                                                )
                                                                .command(
                                                                        "bash",
                                                                        "-c",
                                                                        script.toString()
                                                                )
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                );

        if (response.failures() != null
                && !response.failures().isEmpty()) {

            throw new IllegalStateException(
                    "Failed to start persistent Fargate sandbox: "
                            + response.failures()
            );
        }

        if (response.tasks() == null
                || response.tasks().isEmpty()) {

            throw new IllegalStateException(
                    "Fargate returned no task."
            );
        }

        String taskArn =
                response.tasks()
                        .get(0)
                        .taskArn();

        System.out.println("Persistent sandbox: waiting for RUNNING...");

        waitForRunning(taskArn);

        System.out.println("Persistent sandbox: task is RUNNING.");

        System.out.println("Persistent sandbox: waiting for ExecuteCommandAgent...");

        waitForExecuteCommandAgent(taskArn);

        System.out.println("Persistent sandbox: ExecuteCommandAgent is RUNNING.");

        String runtimeId =
                "runtime-" + UUID.randomUUID();

        return new SandboxRuntime(
                runtimeId,
                taskArn
        );
    }

    @Override
    public SandboxResult execute(SandboxRuntime runtime, String command) {

        if (runtime == null) {
            throw new IllegalArgumentException("Sandbox runtime is required.");
        }

        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException("Sandbox command is required.");
        }

        String wrappedCommand =
                "/bin/sh -c " +
                        "\"cd /workspace/repository && " +
                        "{ " +
                        command +
                        "; " +
                        "rc=\\$?; " +
                        "printf '\\\\n__ENGINEERING_BENCH_EXIT_CODE__=%s\\\\n' \\\"\\$rc\\\"; " +
                        "exit \\$rc; " +
                        "}\"";

        ExecuteCommandRequest request =
                ExecuteCommandRequest.builder()
                        .cluster(CLUSTER)
                        .task(runtime.taskArn())
                        .container(CONTAINER_NAME)
                        .command(wrappedCommand)
                        .interactive(true)
                        .build();

        System.out.println("Calling ECS ExecuteCommand API...");

        ExecuteCommandResponse response =
                ecsClient.executeCommand(request);

        if (response.session() == null) {
            throw new IllegalStateException(
                    "ECS Exec did not return a session."
            );
        }

        System.out.println(
                "ECS ExecuteCommand API returned. Session ID: " +
                        response.session().sessionId()
        );

        return executeSessionManagerPlugin(response, command);
    }

    @Override
    public void stop(
            SandboxRuntime runtime) {

        if (runtime == null) {
            return;
        }

        ecsClient.stopTask(
                StopTaskRequest.builder()
                        .cluster(CLUSTER)
                        .task(runtime.taskArn())
                        .reason(
                                "Engineering Bench workspace completed"
                        )
                        .build()
        );
    }

    // ============================================================
    // Fargate helpers
    // ============================================================

    private SandboxResult runOneShotTask(
            String script) {

        RunTaskResponse response =
                ecsClient.runTask(
                        RunTaskRequest.builder()
                                .cluster(CLUSTER)
                                .taskDefinition(
                                        TASK_DEFINITION
                                )
                                .launchType(
                                        LaunchType.FARGATE
                                )
                                .networkConfiguration(
                                        NetworkConfiguration.builder()
                                                .awsvpcConfiguration(
                                                        AwsVpcConfiguration.builder()
                                                                .subnets(
                                                                        SUBNET_ID
                                                                )
                                                                .securityGroups(
                                                                        SECURITY_GROUP_ID
                                                                )
                                                                .assignPublicIp(
                                                                        AssignPublicIp.ENABLED
                                                                )
                                                                .build()
                                                )
                                                .build()
                                )
                                .overrides(
                                        TaskOverride.builder()
                                                .containerOverrides(
                                                        ContainerOverride.builder()
                                                                .name(
                                                                        CONTAINER_NAME
                                                                )
                                                                .command(
                                                                        "bash",
                                                                        "-c",
                                                                        script
                                                                )
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                );

        if (response.failures() != null
                && !response.failures().isEmpty()) {

            throw new IllegalStateException(
                    "Failed to start Fargate task: "
                            + response.failures()
            );
        }

        String taskArn =
                response.tasks()
                        .get(0)
                        .taskArn();

        DescribeTasksResponse taskResponse =
                waitForTask(taskArn);

        Task task =
                taskResponse.tasks()
                        .get(0);

        Container container =
                task.containers()
                        .get(0);

        Integer exitCode =
                container.exitCode();

        String stdout;

        try {

            stdout =
                    getCloudWatchLogs(taskArn);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Interrupted while retrieving Fargate logs",
                    e
            );
        }

        return new SandboxResult(
                exitCode != null
                        ? exitCode
                        : -1,
                stdout,
                container.reason() != null
                        ? container.reason()
                        : "",
                "",
                ""
        );
    }

    private void waitForRunning(
            String taskArn) {

        while (true) {

            DescribeTasksResponse response =
                    ecsClient.describeTasks(
                            DescribeTasksRequest.builder()
                                    .cluster(CLUSTER)
                                    .tasks(taskArn)
                                    .build()
                    );

            if (response.tasks() == null
                    || response.tasks().isEmpty()) {

                throw new IllegalStateException(
                        "Fargate task disappeared before becoming RUNNING."
                );
            }

            Task task =
                    response.tasks()
                            .get(0);

            String status =
                    task.lastStatus();

            if ("RUNNING".equals(status)) {
                return;
            }

            if ("STOPPED".equals(status)) {

                throw new IllegalStateException(
                        "Persistent Fargate sandbox stopped before becoming RUNNING."
                );
            }

            try {

                Thread.sleep(2000);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                throw new IllegalStateException(
                        "Interrupted while waiting for Fargate sandbox",
                        e
                );
            }
        }
    }

    private DescribeTasksResponse waitForTask(
            String taskArn) {

        while (true) {

            DescribeTasksResponse response =
                    ecsClient.describeTasks(
                            DescribeTasksRequest.builder()
                                    .cluster(CLUSTER)
                                    .tasks(taskArn)
                                    .build()
                    );

            Task task =
                    response.tasks()
                            .get(0);

            if ("STOPPED".equals(
                    task.lastStatus())) {

                return response;
            }

            try {

                Thread.sleep(2000);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                throw new IllegalStateException(
                        "Interrupted while waiting for Fargate task",
                        e
                );
            }
        }
    }

    private String getCloudWatchLogs(
            String taskArn)
            throws InterruptedException {

        String taskId =
                taskArn.substring(
                        taskArn.lastIndexOf("/") + 1
                );

        String logStreamName =
                "sandbox/engineering-bench-sandbox/"
                        + taskId;

        Thread.sleep(3000);

        GetLogEventsResponse response =
                logsClient.getLogEvents(
                        GetLogEventsRequest.builder()
                                .logGroupName(
                                        "/ecs/engineering-bench-sandbox"
                                )
                                .logStreamName(
                                        logStreamName
                                )
                                .startFromHead(true)
                                .build()
                );

        return response.events()
                .stream()
                .map(OutputLogEvent::message)
                .reduce(
                        "",
                        (a, b) -> a + b + "\n"
                );
    }

    private SandboxResult executeSessionManagerPlugin(
            ExecuteCommandResponse response,
            String command) {

        String sessionJson = """
        {
          "SessionId": "%s",
          "StreamUrl": "%s",
          "TokenValue": "%s"
        }
        """.formatted(
                escapeJson(response.session().sessionId()),
                escapeJson(response.session().streamUrl()),
                escapeJson(response.session().tokenValue())
        );

        String taskId =
                response.taskArn()
                        .substring(
                                response.taskArn().lastIndexOf("/") + 1
                        );

        String containerRuntimeId =
                getContainerRuntimeId(response.taskArn());

        String target =
                "ecs:" +
                        CLUSTER +
                        "_" +
                        taskId +
                        "_" +
                        containerRuntimeId;

        String parametersJson =
                "{\"Target\":\"" +
                        escapeJson(target) +
                        "\"}";

        ProcessBuilder processBuilder =
                new ProcessBuilder(
                        "session-manager-plugin",
                        sessionJson,
                        REGION.id(),
                        "StartSession",
                        "",
                        parametersJson,
                        "https://ssm." +
                                REGION.id() +
                                ".amazonaws.com"
                );

        processBuilder.redirectErrorStream(true);

        try {

            System.out.println("Starting session-manager-plugin...");

            Process process =
                    processBuilder.start();

            System.out.println("session-manager-plugin started. Waiting for output...");

            String output =
                    new String(
                            process.getInputStream()
                                    .readAllBytes()
                    );

            System.out.println("session-manager-plugin returned output.");

            int pluginExitCode =
                    process.waitFor();

            System.out.println(
                    "session-manager-plugin exit code: " +
                            pluginExitCode
            );

            Integer remoteExitCode =
                    extractRemoteExitCode(output);

            if (remoteExitCode == null) {

                throw new IllegalStateException(
                        "ECS Exec session completed without an " +
                                "Engineering Bench exit-code marker. " +
                                "Plugin exit code: " +
                                pluginExitCode +
                                ". Output: " +
                                output
                );
            }

            String cleanedOutput =
                    output.replaceAll(
                            "(?m)^__ENGINEERING_BENCH_EXIT_CODE__=.*$",
                            ""
                    ).trim();

            return new SandboxResult(
                    remoteExitCode,
                    cleanedOutput,
                    pluginExitCode == 0
                            ? ""
                            : "Session Manager plugin exited with code "
                            + pluginExitCode,
                    "",
                    command
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to execute session-manager-plugin.",
                    e
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Interrupted while executing ECS Exec command.",
                    e
            );
        }
    }

    private Integer extractRemoteExitCode(String output) {
        String marker = "__ENGINEERING_BENCH_EXIT_CODE__=";

        int markerIndex = output.lastIndexOf(marker);

        if (markerIndex < 0) {
            return null;
        }

        int valueStart = markerIndex + marker.length();

        int valueEnd = output.indexOf('\n', valueStart);

        if (valueEnd < 0) {
            valueEnd = output.length();
        }

        String value = output.substring(valueStart, valueEnd).trim();

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String getContainerRuntimeId(String taskArn) {

        DescribeTasksResponse response =
                ecsClient.describeTasks(
                        DescribeTasksRequest.builder()
                                .cluster(CLUSTER)
                                .tasks(taskArn)
                                .build()
                );

        if (response.tasks() == null
                || response.tasks().isEmpty()) {

            throw new IllegalStateException(
                    "Unable to find ECS task: " + taskArn
            );
        }

        Task task =
                response.tasks().get(0);

        if (task.containers() == null
                || task.containers().isEmpty()) {

            throw new IllegalStateException(
                    "Unable to find ECS container in task: "
                            + taskArn
            );
        }

        Container container =
                task.containers()
                        .stream()
                        .filter(c ->
                                CONTAINER_NAME.equals(c.name())
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Unable to find container: "
                                                + CONTAINER_NAME
                                )
                        );

        if (container.runtimeId() == null
                || container.runtimeId().isBlank()) {

            throw new IllegalStateException(
                    "ECS container runtime ID is unavailable."
            );
        }

        return container.runtimeId();
    }

    private void waitForExecuteCommandAgent(String taskArn) {

        final int maxAttempts = 60;

        String lastTaskStatus = null;
        String lastContainerName = null;
        String lastManagedAgents = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            DescribeTasksResponse response =
                    ecsClient.describeTasks(
                            DescribeTasksRequest.builder()
                                    .cluster(CLUSTER)
                                    .tasks(taskArn)
                                    .build()
                    );

            if (response.tasks() == null
                    || response.tasks().isEmpty()) {

                throw new IllegalStateException(
                        "Fargate task disappeared while waiting for ECS Exec agent."
                );
            }

            Task task = response.tasks().get(0);

            lastTaskStatus = task.lastStatus();

            if ("STOPPED".equals(task.lastStatus())) {

                throw new IllegalStateException(
                        "Fargate task stopped while waiting for ECS Exec agent."
                );
            }

            boolean agentRunning = false;

            if (task.containers() != null) {

                for (Container container : task.containers()) {

                    if (!CONTAINER_NAME.equals(container.name())) {
                        continue;
                    }

                    lastContainerName = container.name();
                    lastManagedAgents =
                            String.valueOf(container.managedAgents());

                    if (container.managedAgents() == null) {
                        continue;
                    }

                    for (ManagedAgent agent : container.managedAgents()) {

                        System.err.println("DEBUG agent.name class = " +
                                (agent.name() == null
                                        ? "null"
                                        : agent.name().getClass().getName()));

                        System.err.println("DEBUG agent.lastStatus class = " +
                                (agent.lastStatus() == null
                                        ? "null"
                                        : agent.lastStatus().getClass().getName()));

                        System.err.println(
                                "DEBUG AGENT NAME=[" + agent.name() + "] " +
                                        "STATUS=[" + agent.lastStatus() + "]"
                        );

                        if (agent.name() == ManagedAgentName.EXECUTE_COMMAND_AGENT
                                && "RUNNING".equals(agent.lastStatus())) {

                            agentRunning = true;
                            break;
                        }
                    }
                }
            }

            if (agentRunning) {
                return;
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                throw new IllegalStateException(
                        "Interrupted while waiting for ECS ExecuteCommandAgent.",
                        e
                );
            }
        }

        throw new IllegalStateException(
                "Timed out waiting for ECS ExecuteCommandAgent to become RUNNING. " +
                        "Last task status=" + lastTaskStatus +
                        ", last container=" + lastContainerName +
                        ", last managed agents=" + lastManagedAgents
        );
    }
}