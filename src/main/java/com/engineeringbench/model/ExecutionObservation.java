package com.engineeringbench.model;

public record ExecutionObservation(
        boolean successful,
        boolean testsExecuted,
        boolean diagnosisRequired,
        String summary
) {
}