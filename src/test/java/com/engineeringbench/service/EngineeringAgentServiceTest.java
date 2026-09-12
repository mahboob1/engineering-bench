package com.engineeringbench.service;

import com.engineeringbench.agent.EngineeringAgent;
import com.engineeringbench.model.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

        ExecutionObservationService observationService =
                mock(ExecutionObservationService.class);

        DiagnosisService diagnosisService =
                mock(DiagnosisService.class);

        EngineeringAgentService service =
                new EngineeringAgentService(
                        toolExecutor,
                        engineeringAgent,
                        repositoryContextService,
                        observationService,
                        diagnosisService
                );

        EngineeringTask task =
                new EngineeringTask(
                        "https://github.com/test/repository.git",
                        "main",
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

        ExecutionObservation observation =
                new ExecutionObservation(
                        true,
                        true,
                        false,
                        "Execution succeeded and tests were executed."
                );

        Diagnosis successfulDiagnosis =
                new Diagnosis(
                        false,
                        "Execution completed successfully.",
                        "BUILD SUCCESSFUL"
                );

        when(toolExecutor.execute(
                "run_command",
                task.repository(),
                task.revision(),
                "./gradlew test"
        )).thenReturn(successfulResult);

        when(observationService.observe(
                successfulResult
        )).thenReturn(observation);

        when(diagnosisService.diagnose(
                successfulResult
        )).thenReturn(successfulDiagnosis);

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

        assertTrue(
                result.contains(
                        "Execution succeeded and tests were executed."
                )
        );

        verify(
                toolExecutor,
                times(1)
        ).execute(
                "run_command",
                task.repository(),
                task.revision(),
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

        verify(
                observationService,
                times(1)
        ).observe(
                successfulResult
        );

        verify(
                diagnosisService,
                times(1)
        ).diagnose(
                successfulResult
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

        ExecutionObservationService observationService =
                mock(ExecutionObservationService.class);

        DiagnosisService diagnosisService =
                mock(DiagnosisService.class);

        EngineeringAgentService service =
                new EngineeringAgentService(
                        toolExecutor,
                        engineeringAgent,
                        repositoryContextService,
                        observationService,
                        diagnosisService
                );

        EngineeringTask task =
                new EngineeringTask(
                        "https://github.com/test/repository.git",
                        "main",
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
                                "Run the tests to evaluate the repository."
                        ),

                        new AgentDecision(
                                "CONTINUE",
                                "run_command",
                                "./gradlew compileJava",
                                "The previous command failed, so compileJava should be attempted."
                        ),

                        new AgentDecision(
                                "STOP",
                                "none",
                                "none",
                                "No further action is required."
                        )
                );

        SandboxResult failedResult =
                new SandboxResult(
                        1,
                        "",
                        "Compilation failed",
                        "",
                        ""
                );

        SandboxResult successfulResult =
                new SandboxResult(
                        0,
                        "BUILD SUCCESSFUL",
                        "",
                        "",
                        ""
                );

        ExecutionObservation failedObservation =
                new ExecutionObservation(
                        false,
                        false,
                        true,
                        "Execution failed. Diagnosis is required."
                );

        ExecutionObservation successfulObservation =
                new ExecutionObservation(
                        true,
                        true,
                        false,
                        "Execution succeeded and tests were executed."
                );

        Diagnosis failedDiagnosis =
                new Diagnosis(
                        true,
                        "The command failed during execution.",
                        "Compilation failed"
                );

        Diagnosis successfulDiagnosis =
                new Diagnosis(
                        false,
                        "Execution completed successfully.",
                        "BUILD SUCCESSFUL"
                );

        when(toolExecutor.execute(
                "run_command",
                task.repository(),
                task.revision(),
                "./gradlew test"
        )).thenReturn(failedResult);

        when(toolExecutor.execute(
                "run_command",
                task.repository(),
                task.revision(),
                "./gradlew compileJava"
        )).thenReturn(successfulResult);

        when(observationService.observe(
                failedResult
        )).thenReturn(failedObservation);

        when(observationService.observe(
                successfulResult
        )).thenReturn(successfulObservation);

        when(diagnosisService.diagnose(
                failedResult
        )).thenReturn(failedDiagnosis);

        when(diagnosisService.diagnose(
                successfulResult
        )).thenReturn(successfulDiagnosis);

        // Act

        String result =
                service.execute(task);

        // Assert

        assertTrue(
                result.contains("Exit Code: 1")
        );

        assertTrue(
                result.contains("./gradlew compileJava")
        );

        assertTrue(
                result.contains("Exit Code: 0")
        );

        assertTrue(
                result.contains("BUILD SUCCESSFUL")
        );

        assertTrue(
                result.contains(
                        "Diagnosis Required: true"
                )
        );

        verify(
                engineeringAgent,
                times(3)
        ).decide(anyString());

        verify(
                toolExecutor,
                times(1)
        ).execute(
                "run_command",
                task.repository(),
                task.revision(),
                "./gradlew test"
        );

        verify(
                toolExecutor,
                times(1)
        ).execute(
                "run_command",
                task.repository(),
                task.revision(),
                "./gradlew compileJava"
        );

        verify(
                observationService,
                times(1)
        ).observe(
                failedResult
        );

        verify(
                observationService,
                times(1)
        ).observe(
                successfulResult
        );

        verify(
                diagnosisService,
                times(1)
        ).diagnose(
                failedResult
        );

        verify(
                diagnosisService,
                times(1)
        ).diagnose(
                successfulResult
        );
    }

    @Test
    void shouldContinueWhenExecutionRequiresDiagnosis() {

        // Arrange

        ToolExecutor toolExecutor =
                mock(ToolExecutor.class);

        EngineeringAgent engineeringAgent =
                mock(EngineeringAgent.class);

        RepositoryContextService repositoryContextService =
                mock(RepositoryContextService.class);

        ExecutionObservationService observationService =
                mock(ExecutionObservationService.class);

        DiagnosisService diagnosisService =
                mock(DiagnosisService.class);

        EngineeringAgentService service =
                new EngineeringAgentService(
                        toolExecutor,
                        engineeringAgent,
                        repositoryContextService,
                        observationService,
                        diagnosisService
                );

        EngineeringTask task =
                new EngineeringTask(
                        "https://github.com/test/repository.git",
                        "main",
                        "Run the repository tests"
                );

        when(repositoryContextService.retrieve(
                task.repository(),
                task.task()
        )).thenReturn(
                "build.gradle contains a Gradle project."
        );

        /*
         * First decision:
         * Agent attempts to run the tests.
         */
        when(engineeringAgent.decide(anyString()))
                .thenReturn(
                        new AgentDecision(
                                "CONTINUE",
                                "run_command",
                                "./gradlew test",
                                "Run the repository tests."
                        ),

                        /*
                         * Second decision:
                         * After seeing the failure observation,
                         * the agent should NOT stop immediately.
                         */
                        new AgentDecision(
                                "CONTINUE",
                                "run_command",
                                "./gradlew compileJava",
                                "The previous execution failed and requires further investigation."
                        ),

                        /*
                         * Third decision:
                         * Agent eventually stops.
                         */
                        new AgentDecision(
                                "STOP",
                                "none",
                                "none",
                                "No further action is required."
                        )
                );

        SandboxResult failedResult =
                new SandboxResult(
                        1,
                        "",
                        "Compilation failed",
                        "",
                        ""
                );

        ExecutionObservation failedObservation =
                new ExecutionObservation(
                        false,
                        false,
                        true,
                        "Execution failed. Diagnosis is required."
                );

        Diagnosis failedDiagnosis =
                new Diagnosis(
                        true,
                        "The command failed during execution.",
                        "Compilation failed"
                );

        SandboxResult successfulResult =
                new SandboxResult(
                        0,
                        "BUILD SUCCESSFUL",
                        "",
                        "",
                        ""
                );

        ExecutionObservation successfulObservation =
                new ExecutionObservation(
                        true,
                        true,
                        false,
                        "Execution succeeded and tests were executed."
                );

        Diagnosis successfulDiagnosis =
                new Diagnosis(
                        false,
                        "Execution completed successfully.",
                        "BUILD SUCCESSFUL"
                );

        when(toolExecutor.execute(
                "run_command",
                task.repository(),
                task.revision(),
                "./gradlew test"
        )).thenReturn(failedResult);

        when(toolExecutor.execute(
                "run_command",
                task.repository(),
                task.revision(),
                "./gradlew compileJava"
        )).thenReturn(successfulResult);

        when(observationService.observe(
                failedResult
        )).thenReturn(failedObservation);

        when(observationService.observe(
                successfulResult
        )).thenReturn(successfulObservation);

        when(diagnosisService.diagnose(
                failedResult
        )).thenReturn(failedDiagnosis);

        when(diagnosisService.diagnose(
                successfulResult
        )).thenReturn(successfulDiagnosis);

        // Act

        String result =
                service.execute(task);

        // Assert

        assertTrue(
                result.contains(
                        "Diagnosis Required: true"
                )
        );

        assertTrue(
                result.contains(
                        "./gradlew compileJava"
                )
        );

        assertTrue(
                result.contains(
                        "Execution failed. Diagnosis is required."
                )
        );

        /*
         * Most important assertion:
         *
         * The agent was called again after the failed execution.
         */
        verify(
                engineeringAgent,
                times(3)
        ).decide(anyString());

        verify(
                toolExecutor,
                times(1)
        ).execute(
                "run_command",
                task.repository(),
                task.revision(),
                "./gradlew test"
        );

        verify(
                toolExecutor,
                times(1)
        ).execute(
                "run_command",
                task.repository(),
                task.revision(),
                "./gradlew compileJava"
        );

        verify(
                observationService,
                times(1)
        ).observe(
                failedResult
        );

        verify(
                diagnosisService,
                times(1)
        ).diagnose(
                failedResult
        );
    }

    @Test
    void shouldProvideDiagnosisToAgentOnNextDecision() {

        // Arrange

        ToolExecutor toolExecutor =
                mock(ToolExecutor.class);

        EngineeringAgent engineeringAgent =
                mock(EngineeringAgent.class);

        RepositoryContextService repositoryContextService =
                mock(RepositoryContextService.class);

        ExecutionObservationService observationService =
                mock(ExecutionObservationService.class);

        DiagnosisService diagnosisService =
                mock(DiagnosisService.class);

        EngineeringAgentService service =
                new EngineeringAgentService(
                        toolExecutor,
                        engineeringAgent,
                        repositoryContextService,
                        observationService,
                        diagnosisService
                );

        EngineeringTask task =
                new EngineeringTask(
                        "https://github.com/test/repository.git",
                        "main",
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
                                "Run the repository tests."
                        ),
                        new AgentDecision(
                                "STOP",
                                "none",
                                "none",
                                "Stop after reviewing the failure."
                        )
                );

        SandboxResult failedResult =
                new SandboxResult(
                        1,
                        "",
                        "Compilation failed",
                        "",
                        ""
                );

        ExecutionObservation failedObservation =
                new ExecutionObservation(
                        false,
                        false,
                        true,
                        "Execution failed. Diagnosis is required."
                );

        Diagnosis diagnosis =
                new Diagnosis(
                        true,
                        "The command failed during execution.",
                        "Compilation failed"
                );

        when(toolExecutor.execute(
                "run_command",
                task.repository(),
                task.revision(),
                "./gradlew test"
        )).thenReturn(failedResult);

        when(observationService.observe(
                failedResult
        )).thenReturn(failedObservation);

        when(diagnosisService.diagnose(
                failedResult
        )).thenReturn(diagnosis);

        // Act

        service.execute(task);

        // Assert

        var decisionCaptor =
                org.mockito.ArgumentCaptor.forClass(
                        String.class
                );

        verify(
                engineeringAgent,
                times(2)
        ).decide(
                decisionCaptor.capture()
        );

        String secondAgentInput =
                decisionCaptor.getAllValues().get(1);

        assertTrue(
                secondAgentInput.contains(
                        "Diagnosis:"
                )
        );

        assertTrue(
                secondAgentInput.contains(
                        "The command failed during execution."
                )
        );

        assertTrue(
                secondAgentInput.contains(
                        "Compilation failed"
                )
        );
    }

    @Test
    void shouldProvideCompilationDiagnosisToAgentOnNextDecision() {

        EngineeringAgent agent =
                mock(EngineeringAgent.class);

        ToolExecutor toolExecutor =
                mock(ToolExecutor.class);

        RepositoryContextService repositoryContextService =
                mock(RepositoryContextService.class);

        ExecutionObservationService observationService =
                new ExecutionObservationService();

        DiagnosisService diagnosisService =
                new DiagnosisService();

        when(repositoryContextService.retrieve(
                anyString(),
                anyString()))
                .thenReturn("Repository evidence");

        when(agent.decide(anyString()))
                .thenReturn(
                        new AgentDecision(
                                "CONTINUE",
                                "run_command",
                                "./gradlew test",
                                "Run tests"
                        ),
                        new AgentDecision(
                                "STOP",
                                "none",
                                "none",
                                "Stop after diagnosis"
                        )
                );

        when(toolExecutor.execute(
                anyString(),
                anyString(),
                anyString(),
                anyString()))
                .thenReturn(
                        new SandboxResult(
                                1,
                                "",
                                """
                                /src/main/java/UserController.java:
                                error: cannot find symbol
                                symbol: class UserService
                                """,
                                "",
                                ""
                        )
                );

        EngineeringAgentService service =
                new EngineeringAgentService(
                        toolExecutor,
                        agent,
                        repositoryContextService,
                        observationService,
                        diagnosisService
                );

        EngineeringTask task =
                new EngineeringTask(
                        "https://github.com/test/repository.git",
                        "main",
                        "Run the repository tests"
                );

        service.execute(task);

        ArgumentCaptor<String> captor =
                ArgumentCaptor.forClass(String.class);

        verify(agent, times(2))
                .decide(captor.capture());

        String secondAgentInput =
                captor.getAllValues().get(1);

        assertTrue(
                secondAgentInput.contains(
                        "COMPILATION_FAILURE"
                )
        );

        assertTrue(
                secondAgentInput.contains(
                        "cannot find symbol"
                )
        );

        assertTrue(
                secondAgentInput.contains(
                        "UserService"
                )
        );
    }

    @Test
    void shouldAllowAgentToChooseRecoveryAfterCompilationFailure() {

        EngineeringAgent agent =
                mock(EngineeringAgent.class);

        ToolExecutor toolExecutor =
                mock(ToolExecutor.class);

        RepositoryContextService repositoryContextService =
                mock(RepositoryContextService.class);

        ExecutionObservationService observationService =
                new ExecutionObservationService();

        DiagnosisService diagnosisService =
                new DiagnosisService();

        when(repositoryContextService.retrieve(
                anyString(),
                anyString()))
                .thenReturn("Repository evidence");

        when(agent.decide(anyString()))
                .thenReturn(
                        new AgentDecision(
                                "CONTINUE",
                                "run_command",
                                "./gradlew test",
                                "Run tests"
                        ),
                        new AgentDecision(
                                "CONTINUE",
                                "run_command",
                                "./gradlew compileJava",
                                "Re-run compilation after diagnosing the failure"
                        ),
                        new AgentDecision(
                                "STOP",
                                "none",
                                "none",
                                "Stop"
                        )
                );

        when(toolExecutor.execute(
                anyString(),
                anyString(),
                anyString(),
                anyString()))
                .thenReturn(
                        new SandboxResult(
                                1,
                                "",
                                """
                                error: cannot find symbol
                                symbol: class UserService
                                """,
                                "",
                                ""
                        )
                );

        EngineeringAgentService service =
                new EngineeringAgentService(
                        toolExecutor,
                        agent,
                        repositoryContextService,
                        observationService,
                        diagnosisService
                );

        EngineeringTask task =
                new EngineeringTask(
                        "https://github.com/test/repository.git",
                        "main",
                        "Run the repository tests"
                );

        service.execute(task);

        verify(toolExecutor, times(2))
                .execute(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );

        verify(toolExecutor)
                .execute(
                        anyString(),
                        anyString(),
                        anyString(),
                        eq("./gradlew compileJava")
                );
    }
}