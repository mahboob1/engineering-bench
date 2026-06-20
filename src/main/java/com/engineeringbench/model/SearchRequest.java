package com.engineeringbench.model;

public record SearchRequest(
        String sessionId,
        String question,
        String repository
) {
}