package com.engineeringbench.model;

public record WorkspaceTask(
        String id,
        String workspaceId,
        String task,
        WorkspaceTaskStatus status
) {

    public WorkspaceTask {
        if (status == null) {
            status = WorkspaceTaskStatus.CREATED;
        }
    }

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