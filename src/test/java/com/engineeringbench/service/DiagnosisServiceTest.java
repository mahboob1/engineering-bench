package com.engineeringbench.service;

import com.engineeringbench.model.Diagnosis;
import com.engineeringbench.model.SandboxResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiagnosisServiceTest {

    @Test
    void shouldDiagnoseFailedExecution() {

        // Arrange

        DiagnosisService service =
                new DiagnosisService();

        SandboxResult result =
                new SandboxResult(
                        1,
                        "",
                        "Process terminated unexpectedly",
                        "",
                        ""
                );

        // Act

        Diagnosis diagnosis =
                service.diagnose(result);

        // Assert

        assertTrue(
                diagnosis.required()
        );

        assertEquals(
                "The command failed during execution.",
                diagnosis.summary()
        );

        assertEquals(
                "Process terminated unexpectedly",
                diagnosis.evidence()
        );
    }


    @Test
    void shouldNotRequireDiagnosisForSuccessfulExecution() {

        // Arrange

        DiagnosisService service =
                new DiagnosisService();

        SandboxResult result =
                new SandboxResult(
                        0,
                        "BUILD SUCCESSFUL",
                        "",
                        "",
                        ""
                );

        // Act

        Diagnosis diagnosis =
                service.diagnose(result);

        // Assert

        assertFalse(
                diagnosis.required()
        );

        assertEquals(
                "Execution completed successfully.",
                diagnosis.summary()
        );

        assertEquals(
                "BUILD SUCCESSFUL",
                diagnosis.evidence()
        );
    }

    @Test
    void shouldExtractMeaningfulDiagnosisFromCompilationFailure() {

        // Arrange

        DiagnosisService service =
                new DiagnosisService();

        SandboxResult result =
                new SandboxResult(
                        1,
                        "",
                        """
                        /workspace/repository/src/main/java/UserController.java:
                        error: cannot find symbol
                        symbol: class UserService
                        location: class UserController
                        """,
                        "",
                        ""
                );

        // Act

        Diagnosis diagnosis =
                service.diagnose(result);

        // Assert

        assertTrue(
                diagnosis.required()
        );

        assertTrue(
                diagnosis.summary()
                        .toLowerCase()
                        .contains("compilation")
        );

        assertTrue(
                diagnosis.evidence()
                        .contains("cannot find symbol")
        );

        assertTrue(
                diagnosis.evidence()
                        .contains("UserService")
        );
    }

    @Test
    void shouldClassifyCompilationFailure() {

        // Arrange

        DiagnosisService service =
                new DiagnosisService();

        SandboxResult result =
                new SandboxResult(
                        1,
                        "",
                        """
                        /workspace/repository/src/main/java/UserController.java:
                        error: cannot find symbol
                        symbol: class UserService
                        location: class UserController
                        """,
                        "",
                        ""
                );

        // Act

        Diagnosis diagnosis =
                service.diagnose(result);

        // Assert

        assertEquals(
                "COMPILATION_FAILURE",
                diagnosis.summary()
        );
    }
}