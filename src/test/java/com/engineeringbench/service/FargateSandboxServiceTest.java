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

            SandboxResult result =
                    service.execute(
                            runtime,
                            "echo ECS_EXEC_PROGRAMMATIC_WORKS"
                    );

            assertNotNull(result);

            System.out.println("========================================");
            System.out.println("PROGRAMMATIC ECS EXEC TEST");
            System.out.println("Exit Code: " + result.exitCode());
            System.out.println("Output:");
            System.out.println(result.stdout());
            System.out.println("========================================");

            assertEquals(
                    0,
                    result.exitCode()
            );

            assertTrue(
                    result.stdout()
                            .contains("ECS_EXEC_PROGRAMMATIC_WORKS")
            );

        } finally {

            if (runtime != null) {
                service.stop(runtime);
            }
        }
    }


}