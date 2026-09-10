package com.engineeringbench.model;

public record AgentDecision(
        String action,
        String toolName,
        String command,
        String reasoning
) {
}