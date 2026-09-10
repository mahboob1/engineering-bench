package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringTask;
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

        String toolResult =
                toolExecutor.execute(
                        toolName,
                        task.repository(),
                        command
                );

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
                %s
                """.formatted(
                task.repository(),
                task.task(),
                toolName,
                command,
                toolResult
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
}