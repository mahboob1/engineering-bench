package com.engineeringbench.service;

import com.engineeringbench.model.AgentDecision;
import com.engineeringbench.model.EngineeringTask;
import com.engineeringbench.model.SandboxResult;
import org.springframework.stereotype.Service;

@Service
public class EngineeringAgentService {

    private final ToolExecutor toolExecutor;

    public EngineeringAgentService(
            ToolExecutor toolExecutor) {

        this.toolExecutor = toolExecutor;
    }

    public String execute(EngineeringTask task) {

        AgentDecision decision =
                createDecision(task);

        SandboxResult result =
                toolExecutor.execute(
                        decision.toolName(),
                        task.repository(),
                        decision.command()
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
                Tool: %s
                Command: %s
                Reasoning: %s

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
                decision.toolName(),
                decision.command(),
                decision.reasoning(),
                result.exitCode(),
                result.successful(),
                observation,
                result.stdout(),
                result.stderr()
        );
    }

    private AgentDecision createDecision(
            EngineeringTask task) {

        String taskText =
                task.task().toLowerCase();

        if (taskText.contains("test")
                || taskText.contains("tests")) {

            return new AgentDecision(
                    "run_command",
                    "./gradlew test",
                    "The task requests test execution."
            );
        }

        if (taskText.contains("build")) {

            return new AgentDecision(
                    "run_command",
                    "./gradlew build",
                    "The task requests a repository build."
            );
        }

        if (taskText.contains("compile")) {

            return new AgentDecision(
                    "run_command",
                    "./gradlew compileJava",
                    "The task requests Java compilation."
            );
        }

        throw new IllegalArgumentException(
                "Agent could not determine an execution action for task: "
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