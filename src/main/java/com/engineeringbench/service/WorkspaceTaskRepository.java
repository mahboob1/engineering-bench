package com.engineeringbench.service;

import com.engineeringbench.model.WorkspaceTask;

import java.util.List;
import java.util.Optional;

public interface WorkspaceTaskRepository {

    WorkspaceTask save(WorkspaceTask task);

    Optional<WorkspaceTask> findById(String id);

    List<WorkspaceTask> findAll();

    List<WorkspaceTask> findByWorkspaceId(String workspaceId);

    void deleteById(String id);

    long count();
}