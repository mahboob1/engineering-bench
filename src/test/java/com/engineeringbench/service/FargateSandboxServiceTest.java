package com.engineeringbench.service;

import com.engineeringbench.model.SandboxResult;
import com.engineeringbench.model.SandboxRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FargateSandboxServiceTest {

    @Test
    void shouldStartAndStopPersistentSandbox() {

        FargateSandboxService service =
                new FargateSandboxService();

        SandboxRuntime runtime = null;

        try {
            runtime = service.start(
                    "https://github.com/mahboob1/engineering-bench.git",
                    null
            );

            assertNotNull(runtime);
            assertNotNull(runtime.id());
            assertNotNull(runtime.taskArn());

            System.out.println(
                    "Persistent sandbox runtime:"
            );
            System.out.println(
                    "Runtime ID: " + runtime.id()
            );
            System.out.println(
                    "Task ARN: " + runtime.taskArn()
            );

        } finally {

            if (runtime != null) {
                service.stop(runtime);
            }
        }
    }

    @Test
    void shouldExecuteCommandInPersistentSandbox() {

        FargateSandboxService service =
                new FargateSandboxService();

        SandboxRuntime runtime = null;

        try {

            runtime =
                    service.start(
                            "https://github.com/mahboob1/engineering-bench.git",
                            null
                    );

            assertNotNull(runtime);
            assertNotNull(runtime.taskArn());

            SandboxResult firstResult =
                    service.execute(
                            runtime,
                            "echo PERSISTENT_RUNTIME_FIRST"
                    );

            SandboxResult secondResult =
                    service.execute(
                            runtime,
                            "echo PERSISTENT_RUNTIME_SECOND"
                    );

            assertNotNull(firstResult);
            assertNotNull(secondResult);

            System.out.println("========================================");
            System.out.println("PERSISTENT ECS EXEC TEST");
            System.out.println("Runtime ID: " + runtime.id());
            System.out.println("Task ARN: " + runtime.taskArn());

            System.out.println("First command:");
            System.out.println("Exit Code: " + firstResult.exitCode());
            System.out.println("Output:");
            System.out.println(firstResult.stdout());

            System.out.println("Second command:");
            System.out.println("Exit Code: " + secondResult.exitCode());
            System.out.println("Output:");
            System.out.println(secondResult.stdout());
            System.out.println("========================================");

            assertEquals(
                    0,
                    firstResult.exitCode()
            );

            assertEquals(
                    0,
                    secondResult.exitCode()
            );

            assertTrue(
                    firstResult.stdout()
                            .contains("PERSISTENT_RUNTIME_FIRST")
            );

            assertTrue(
                    secondResult.stdout()
                            .contains("PERSISTENT_RUNTIME_SECOND")
            );

        } finally {

            if (runtime != null) {
                service.stop(runtime);
            }
        }
    }

    @Test
    void shouldInitializeWorkingRepository() {

        FargateSandboxService service =
                new FargateSandboxService();

        SandboxResult result =
                service.initializeWorkingRepository(
                        "https://github.com/spring-projects/spring-petclinic.git",
                        "https://github.com/mahboob1/engineering-bench-petclinic.git"
                );

        assertNotNull(result);

        System.out.println("========================================");
        System.out.println("WORKING REPOSITORY INITIALIZATION TEST");
        System.out.println("Exit Code: " + result.exitCode());
        System.out.println("Output:");
        System.out.println(result.stdout());
        System.out.println("========================================");

        assertEquals(0, result.exitCode());

        assertTrue(
                result.stdout()
                        .contains("Working repository initialized successfully.")
        );
    }

}