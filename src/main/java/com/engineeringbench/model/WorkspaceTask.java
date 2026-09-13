package com.engineeringbench.model;

public record WorkspaceTask(
        String id,
        String workspaceId,
        String task,
        WorkspaceTaskStatus status
) {

    public WorkspaceTask(
            String id,
            String workspaceId,
            String task) {

        this(
                id,
                workspaceId,
                task,
                WorkspaceTaskStatus.CREATED
        );
    }
}