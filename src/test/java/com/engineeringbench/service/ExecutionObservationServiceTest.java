package com.engineeringbench.service;

import com.engineeringbench.model.ExecutionObservation;
import com.engineeringbench.model.SandboxResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionObservationServiceTest {

    private final ExecutionObservationService service =
            new ExecutionObservationService();


    @Test
    void shouldIdentifySuccessfulExecutionWithNoTests() {

        // Arrange

        SandboxResult result =
                new SandboxResult(
                        0,
                        "BUILD SUCCESSFUL\n"
                                + "> Task :test NO-SOURCE",
                        "",
                        "",
                        ""
                );

        // Act

        ExecutionObservation observation =
                service.observe(result);

        // Assert

        assertTrue(observation.successful());

        assertFalse(observation.testsExecuted());

        assertFalse(observation.diagnosisRequired());

        assertEquals(
                "Execution succeeded, but no tests were executed.",
                observation.summary()
        );
    }


    @Test
    void shouldIdentifyFailedExecutionRequiringDiagnosis() {

        // Arrange

        SandboxResult result =
                new SandboxResult(
                        1,
                        "",
                        "Compilation failed",
                        "",
                        ""
                );

        // Act

        ExecutionObservation observation =
                service.observe(result);

        // Assert

        assertFalse(observation.successful());

        assertFalse(observation.testsExecuted());

        assertTrue(observation.diagnosisRequired());

        assertEquals(
                "Execution failed. Diagnosis is required.",
                observation.summary()
        );
    }


    @Test
    void shouldIdentifySuccessfulExecutionWithTests() {

        // Arrange

        SandboxResult result =
                new SandboxResult(
                        0,
                        """
                        > Task :test

                        Test results:
                        5 tests completed, 0 failed

                        BUILD SUCCESSFUL
                        """,
                        "",
                        "",
                        ""
                );

        // Act

        ExecutionObservation observation =
                service.observe(result);

        // Assert

        assertTrue(observation.successful());

        assertTrue(observation.testsExecuted());

        assertFalse(observation.diagnosisRequired());

        assertEquals(
                "Execution succeeded and tests were executed.",
                observation.summary()
        );
    }
}