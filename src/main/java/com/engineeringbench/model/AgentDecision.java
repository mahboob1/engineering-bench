package com.engineeringbench.model;

public record AgentDecision(
        String toolName,
        String command,
        String reasoning
) {
}