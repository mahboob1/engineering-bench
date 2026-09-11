package com.engineeringbench.model;

public record Diagnosis(
        boolean required,
        String summary,
        String evidence
) {
}