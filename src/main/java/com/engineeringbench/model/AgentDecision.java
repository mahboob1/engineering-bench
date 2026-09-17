package com.engineeringbench.model;

import com.fasterxml.jackson.databind.JsonNode;

public record AgentDecision(
        String action,
        String toolName,
        JsonNode command,
        String reasoning
) {
}