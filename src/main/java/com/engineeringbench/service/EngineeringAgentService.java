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

        String plan = createPlan(task);

        SandboxResult result =
                sandboxService.execute(
                        task.repository(),
                        List.of("build/test")
                );

        return """
                Engineering Task
                -----------------
                Repository: %s
                Task: %s

                Plan
                ----
                %s

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
                result.exitCode(),
                result.successful(),
                result.stdout(),
                result.stderr()
        );
    }

    private String createPlan(
            EngineeringTask task) {

        return """
                1. Understand the requested change.
                2. Locate the relevant source files.
                3. Modify the repository.
                4. Run the build and tests.
                5. Verify the resulting change.
                """;
    }
}