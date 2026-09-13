package com.engineeringbench.model;

public record WorkspaceTask(
        String id,
        String workspaceId,
        String task
) {
}