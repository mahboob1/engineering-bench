package com.engineeringbench.model;

public record ChatRequest(
        String sessionId,
        String question,
        String repository
) {
}