package com.engineeringbench.service;

import com.engineeringbench.model.SandboxResult;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.*;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import java.util.List;

@Service("fargateSandboxService")
public class FargateSandboxService implements SandboxService {

    private final EcsClient ecsClient;
    private final CloudWatchLogsClient logsClient;

    public FargateSandboxService() {
        this.ecsClient = EcsClient.builder()
                .region(Region.US_EAST_2)
                .build();

        this.logsClient = CloudWatchLogsClient.builder()
                .region(Region.US_EAST_2)
                .build();
    }

    @Override
    public SandboxResult execute(
            String repository,
            List<String> commands) {

        StringBuilder script = new StringBuilder();

        script.append("""
        set -e

        echo "Cloning repository..."
        git clone %s /workspace/repository

        echo "Repository cloned successfully"
        cd /workspace/repository

        echo "Inspecting repository..."
        echo "Repository files:"
        find . -maxdepth 2 -type f | sort

        echo "Build configuration files:"
        for file in \\
            gradlew \\
            build.gradle \\
            build.gradle.kts \\
            pom.xml \\
            mvnw \\
            package.json \\
            pyproject.toml \\
            requirements.txt
        do
            if [ -f "$file" ]; then
                echo "FOUND: $file"
            fi
        done

        echo "Detecting build system..."

        if [ -f "gradlew" ]; then
            BUILD_SYSTEM="Gradle"
            TEST_COMMAND="./gradlew test"
        elif [ -f "mvnw" ]; then
            BUILD_SYSTEM="Maven"
            TEST_COMMAND="./mvnw test"
        elif [ -f "pom.xml" ]; then
            BUILD_SYSTEM="Maven"
            TEST_COMMAND="mvn test"
        elif [ -f "package.json" ]; then
            BUILD_SYSTEM="Node.js"
            TEST_COMMAND="npm test"
        elif [ -f "pyproject.toml" ]; then
            BUILD_SYSTEM="Python"
            TEST_COMMAND="pytest"
        elif [ -f "requirements.txt" ]; then
            BUILD_SYSTEM="Python"
            TEST_COMMAND="pytest"
        else
            BUILD_SYSTEM="Unknown"
            TEST_COMMAND=""
        fi

        echo "Build System: $BUILD_SYSTEM"
        echo "Test Command: $TEST_COMMAND"
        
        if [ -n "$TEST_COMMAND" ]; then
            echo "Executing detected test command..."
            echo ">>> $TEST_COMMAND"
            eval "$TEST_COMMAND"
            echo "Detected test command completed successfully"
        else
            echo "No supported test command detected"
            exit 1
        fi

        """.formatted(repository));

        script.append("echo \"Executing commands...\"\n");

        for (String command : commands) {
            script.append("echo \">>> ").append(command).append("\"\n");
            script.append(command).append("\n");
        }

        script.append("""
                
                echo "All commands completed successfully"
                """);

        RunTaskRequest request = RunTaskRequest.builder()
                .cluster("engineering-bench")
                .taskDefinition("engineering-bench-sandbox:1")
                .launchType(LaunchType.FARGATE)
                .networkConfiguration(
                        NetworkConfiguration.builder()
                                .awsvpcConfiguration(
                                        AwsVpcConfiguration.builder()
                                                .subnets("subnet-0f2014463b95a7c00")
                                                .securityGroups("sg-05db641b523a00737")
                                                .assignPublicIp(AssignPublicIp.ENABLED)
                                                .build())
                                .build())
                .overrides(
                        TaskOverride.builder()
                                .containerOverrides(
                                        ContainerOverride.builder()
                                                .name("engineering-bench-sandbox")
                                                .command("bash", "-c", script.toString())
                                                .build())
                                .build())
                .build();

        RunTaskResponse response = ecsClient.runTask(request);

        if (response.failures() != null && !response.failures().isEmpty()) {
            throw new IllegalStateException(
                    "Failed to start Fargate task: " + response.failures());
        }

        String taskArn = response.tasks().get(0).taskArn();

        DescribeTasksResponse taskResponse = waitForTask(taskArn);

        Task task = taskResponse.tasks().get(0);

        Container container = task.containers().get(0);

        Integer exitCode = container.exitCode();

        String stdout;

        try {
            stdout = getCloudWatchLogs(taskArn);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while retrieving Fargate logs", e);
        }

        String buildSystem =
                extractValue(stdout, "Build System:");

        String testCommand =
                extractValue(stdout, "Test Command:");

        return new SandboxResult(
                exitCode != null ? exitCode : -1,
                stdout,
                container.reason() != null ? container.reason() : "",
                buildSystem,
                testCommand
        );
    }

    private DescribeTasksResponse waitForTask(String taskArn) {

        while (true) {

            DescribeTasksResponse response = ecsClient.describeTasks(
                    DescribeTasksRequest.builder()
                            .cluster("engineering-bench")
                            .tasks(taskArn)
                            .build());

            Task task = response.tasks().get(0);

            if ("STOPPED".equals(task.lastStatus())) {
                return response;
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(
                        "Interrupted while waiting for Fargate task", e);
            }
        }
    }

    private String getCloudWatchLogs(String taskArn)
            throws InterruptedException {

        String taskId =
                taskArn.substring(taskArn.lastIndexOf("/") + 1);

        String logStreamName =
                "sandbox/engineering-bench-sandbox/" + taskId;

        Thread.sleep(3000);

        GetLogEventsResponse response = logsClient.getLogEvents(
                GetLogEventsRequest.builder()
                        .logGroupName("/ecs/engineering-bench-sandbox")
                        .logStreamName(logStreamName)
                        .startFromHead(true)
                        .build()
        );

        return response.events().stream()
                .map(OutputLogEvent::message)
                .reduce("", (a, b) -> a + b + "\n");
    }

    private String extractValue(String output, String prefix) {

        return output.lines()
                .filter(line -> line.startsWith(prefix))
                .map(line -> line.substring(prefix.length()).trim())
                .findFirst()
                .orElse("");
    }
}