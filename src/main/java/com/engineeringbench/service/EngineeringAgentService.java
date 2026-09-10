package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringTask;
import com.engineeringbench.model.SandboxResult;
import com.engineeringbench.tool.EngineeringTool;
import org.springframework.stereotype.Service;

@Service
public class EngineeringAgentService {

    private final ToolExecutor toolExecutor;

    public EngineeringAgentService(
            ToolExecutor toolExecutor) {

        this.toolExecutor = toolExecutor;
    }

    public String execute(EngineeringTask task) {

        String command =
                chooseCommand(task);

        String toolName =
                "run_command";

        SandboxResult result =
                toolExecutor.execute(
                        toolName,
                        task.repository(),
                        command
                );

        String observation =
                observe(result);

        return """
                Engineering Task
                -----------------
                Repository: %s
                Task: %s

                Agent Decision
                --------------
                Selected Tool: %s
                Selected Command: %s

                Tool Execution
                --------------
                Exit Code: %d
                Successful: %s

                Agent Observation
                -----------------
                %s

                STDOUT
                ------
                %s

                STDERR
                ------
                %s
                """.formatted(
                task.repository(),
                task.task(),
                toolName,
                command,
                result.exitCode(),
                result.successful(),
                observation,
                result.stdout(),
                result.stderr()
        );
    }

    private String chooseCommand(
            EngineeringTask task) {

        String taskText =
                task.task().toLowerCase();

        if (taskText.contains("test")
                || taskText.contains("tests")) {

            return "./gradlew test";
        }

        if (taskText.contains("build")) {

            return "./gradlew build";
        }

        if (taskText.contains("compile")) {

            return "./gradlew compileJava";
        }

        throw new IllegalArgumentException(
                "Agent could not determine an execution command for task: "
                        + task.task()
        );
    }

    private String observe(
            SandboxResult result) {

        if (result.exitCode() == 0) {

            if (result.stdout().contains(
                    "NO-SOURCE")) {

                return """
                        Execution completed successfully,
                        but no test source files were found.
                        The command succeeded, but this does not
                        prove that tests actually executed.
                        """;
            }

            return """
                    Execution completed successfully.
                    The selected command returned exit code 0.
                    """;
        }

        return """
                Execution failed.
                The selected command returned a non-zero exit code.
                Further diagnosis is required.
                """;
    }
}