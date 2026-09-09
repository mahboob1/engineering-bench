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
            @Qualifier("fargateSandboxService") SandboxService sandboxService) {

        this.sandboxService = sandboxService;
    }

    public String execute(
            EngineeringTask task) {

        /*
         * Phase 1:
         * Establish the engineering task → sandbox flow.
         *
         * The actual LLM planning and code modification
         * will be added in the next phase.
         */

        List<String> commands = List.of("./gradlew test");

        SandboxResult result =
                sandboxService.execute(
                        task.repository(),
                        commands
                );

        String plan = createPlan(task, result);

        return """
        Engineering Task
        -----------------
        Repository: %s
        Task: %s

        Plan
        ----
        %s

        Repository Detection
        ---------------------
        Build System: %s
        Test Command: %s

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
                plan,
                result.buildSystem(),
                result.testCommand(),
                result.exitCode(),
                result.successful(),
                result.stdout(),
                result.stderr()
        );
    }

    private String createPlan(
            EngineeringTask task,
            SandboxResult result) {

        if (result.buildSystem().isBlank()) {
            return """
                1. Inspect the repository.
                2. Determine the repository build system.
                3. No supported build system was detected.
                """;
        }

        return """
            1. Inspect the repository.
            2. Detect the repository build system.
            3. Execute the detected test command.
            4. Observe the execution result.
            5. Verify the command completed successfully.

            Detected Build System: %s
            Detected Test Command: %s
            """.formatted(
                result.buildSystem(),
                result.testCommand()
        );
    }
}