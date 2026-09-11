package com.engineeringbench.service;

import com.engineeringbench.model.ExecutionObservation;
import com.engineeringbench.model.SandboxResult;
import org.springframework.stereotype.Service;

@Service
public class ExecutionObservationService {

    public ExecutionObservation observe(
            SandboxResult result) {

        boolean successful =
                result.exitCode() == 0;

        boolean testsExecuted =
                successful
                        && !result.stdout().contains("NO-SOURCE");

        boolean diagnosisRequired =
                !successful;

        String summary;

        if (!successful) {

            summary =
                    "Execution failed. Diagnosis is required.";

        } else if (!testsExecuted) {

            summary =
                    "Execution succeeded, but no tests were executed.";

        } else {

            summary =
                    "Execution succeeded and tests were executed.";
        }

        return new ExecutionObservation(
                successful,
                testsExecuted,
                diagnosisRequired,
                summary
        );
    }
}