package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringTask;
import com.engineeringbench.model.SandboxResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EngineeringAgentService {

    private final SandboxService sandboxService;

    public EngineeringAgentService(
            @Qualifier("fargateSandboxService")
            SandboxService sandboxService) {

        this.sandboxService = sandboxService;
    }

    public String execute(EngineeringTask task) {

        String command = chooseCommand(task);

        List<String> commands =
                List.of(command);

        SandboxResult result =
                sandboxService.execute(
                        task.repository(),
                        commands
                );

        return """
                Engineering Task
                -----------------
                Repository: %s
                Task: %s

                Agent Decision
                --------------
                Selected Command: %s

                Sandbox Result
                --------------
                Exit Code: %d
                Successful: %s

                STDOUT
                ------
                %s

                STDERR
                ------
                %s
                """.formatted(
                task.repository(),
                task.task(),
                command,
                result.exitCode(),
                result.successful(),
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
}