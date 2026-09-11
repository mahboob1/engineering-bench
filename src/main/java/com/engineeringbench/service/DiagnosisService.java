package com.engineeringbench.service;

import com.engineeringbench.model.Diagnosis;
import com.engineeringbench.model.SandboxResult;
import org.springframework.stereotype.Service;

@Service
public class DiagnosisService {

    public Diagnosis diagnose(
            SandboxResult result) {

        if (result.exitCode() == 0) {

            return new Diagnosis(
                    false,
                    "Execution completed successfully.",
                    result.stdout()
            );
        }

        String stderr = result.stderr();

        if (isCompilationFailure(stderr)) {

            return new Diagnosis(
                    true,
                    "COMPILATION_FAILURE",
                    stderr
            );
        }

        return new Diagnosis(
                true,
                "The command failed during execution.",
                stderr
        );
    }

    private boolean isCompilationFailure(
            String stderr) {

        return stderr.contains("error:")
                || stderr.contains("cannot find symbol")
                || stderr.contains("Compilation failed");
    }
}