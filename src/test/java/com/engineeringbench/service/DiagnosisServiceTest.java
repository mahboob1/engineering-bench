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
                        "Compilation failed",
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
                "Compilation failed",
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
}