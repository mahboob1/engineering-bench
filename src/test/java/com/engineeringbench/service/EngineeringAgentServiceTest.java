package com.engineeringbench.service;

import com.engineeringbench.agent.EngineeringAgent;
import com.engineeringbench.model.AgentDecision;
import com.engineeringbench.model.EngineeringTask;
import com.engineeringbench.model.SandboxResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EngineeringAgentServiceTest {

    @Test
    void shouldExecuteCommandAndStopWhenAgentSaysStop() {

        // Arrange

        ToolExecutor toolExecutor =
                mock(ToolExecutor.class);

        EngineeringAgent engineeringAgent =
                mock(EngineeringAgent.class);

        RepositoryContextService repositoryContextService =
                mock(RepositoryContextService.class);

        EngineeringAgentService service =
                new EngineeringAgentService(
                        toolExecutor,
                        engineeringAgent,
                        repositoryContextService
                );

        EngineeringTask task =
                new EngineeringTask(
                        "https://github.com/mahboob1/engineering-bench.git",
                        "Run the repository tests"
                );

        when(repositoryContextService.retrieve(
                task.repository(),
                task.task()
        )).thenReturn(
                "build.gradle contains a Gradle project."
        );

        when(engineeringAgent.decide(anyString()))
                .thenReturn(
                        new AgentDecision(
                                "CONTINUE",
                                "run_command",
                                "./gradlew test",
                                "The task requires running the repository tests."
                        ),
                        new AgentDecision(
                                "STOP",
                                "none",
                                "none",
                                "The test command completed successfully."
                        )
                );

        SandboxResult successfulResult =
                new SandboxResult(
                        0,
                        "BUILD SUCCESSFUL",
                        "",
                        "",
                        ""
                );

        when(toolExecutor.execute(
                "run_command",
                task.repository(),
                "./gradlew test"
        )).thenReturn(successfulResult);

        // Act

        String result =
                service.execute(task);

        // Assert

        assertTrue(
                result.contains("Action: CONTINUE")
        );

        assertTrue(
                result.contains("Action: STOP")
        );

        assertTrue(
                result.contains("./gradlew test")
        );

        assertTrue(
                result.contains("BUILD SUCCESSFUL")
        );

        verify(
                toolExecutor,
                times(1)
        ).execute(
                "run_command",
                task.repository(),
                "./gradlew test"
        );

        verify(
                engineeringAgent,
                times(2)
        ).decide(anyString());

        verify(
                repositoryContextService,
                times(1)
        ).retrieve(
                task.repository(),
                task.task()
        );
    }


    @Test
    void shouldAllowAgentToChooseAnotherCommandAfterFailure() {

        // Arrange

        ToolExecutor toolExecutor =
                mock(ToolExecutor.class);

        EngineeringAgent engineeringAgent =
                mock(EngineeringAgent.class);

        RepositoryContextService repositoryContextService =
                mock(RepositoryContextService.class);

        EngineeringAgentService service =
                new EngineeringAgentService(
                        toolExecutor,
                        engineeringAgent,
                        repositoryContextService
                );

        EngineeringTask task =
                new EngineeringTask(
                        "https://github.com/mahboob1/engineering-bench.git",
                        "Build the repository"
                );

        when(repositoryContextService.retrieve(
                task.repository(),
                task.task()
        )).thenReturn(
                "build.gradle contains a Gradle project."
        );

        /*
         * Agent #1:
         * Try the test command.
         */
        when(engineeringAgent.decide(anyString()))
                .thenReturn(
                        new AgentDecision(
                                "CONTINUE",
                                "run_command",
                                "./gradlew test",
                                "Run the tests to evaluate the repository."
                        ),

                        /*
                         * Agent #2:
                         * The first command failed, so choose another
                         * command.
                         */
                        new AgentDecision(
                                "CONTINUE",
                                "run_command",
                                "./gradlew compileJava",
                                "The previous command failed, so compileJava should be attempted."
                        ),

                        /*
                         * Agent #3:
                         * Stop after the second execution.
                         */
                        new AgentDecision(
                                "STOP",
                                "none",
                                "none",
                                "No further action is required."
                        )
                );

        /*
         * First command fails.
         */
        SandboxResult failedResult =
                new SandboxResult(
                        1,
                        "",
                        "Compilation failed",
                        "",
                        ""
                );

        /*
         * Second command succeeds.
         */
        SandboxResult successfulResult =
                new SandboxResult(
                        0,
                        "BUILD SUCCESSFUL",
                        "",
                        "",
                        ""
                );

        when(toolExecutor.execute(
                "run_command",
                task.repository(),
                "./gradlew test"
        )).thenReturn(failedResult);

        when(toolExecutor.execute(
                "run_command",
                task.repository(),
                "./gradlew compileJava"
        )).thenReturn(successfulResult);

        // Act

        String result =
                service.execute(task);

        // Assert

        /*
         * The first command should have failed.
         */
        assertTrue(
                result.contains("Exit Code: 1")
        );

        /*
         * The second command should have been selected.
         */
        assertTrue(
                result.contains("./gradlew compileJava")
        );

        /*
         * The second command should have succeeded.
         */
        assertTrue(
                result.contains("Exit Code: 0")
        );

        assertTrue(
                result.contains("BUILD SUCCESSFUL")
        );

        /*
         * Agent should have made three decisions:
         *
         * 1. CONTINUE → ./gradlew test
         * 2. CONTINUE → ./gradlew compileJava
         * 3. STOP
         */
        verify(
                engineeringAgent,
                times(3)
        ).decide(anyString());

        /*
         * First command executed exactly once.
         */
        verify(
                toolExecutor,
                times(1)
        ).execute(
                "run_command",
                task.repository(),
                "./gradlew test"
        );

        /*
         * Second command executed exactly once.
         */
        verify(
                toolExecutor,
                times(1)
        ).execute(
                "run_command",
                task.repository(),
                "./gradlew compileJava"
        );
    }
}