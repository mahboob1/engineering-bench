package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringTask;
import com.engineeringbench.model.WorkspaceTask;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkspaceTaskService {

    private final Map<String, WorkspaceTask> tasks =
            new ConcurrentHashMap<>();

    private final EngineeringWorkspaceService workspaceService;

    public WorkspaceTaskService(
            EngineeringWorkspaceService workspaceService) {

        this.workspaceService = workspaceService;
    }

    public WorkspaceTask create(
            WorkspaceTask task) {

        if (!workspaceService.exists(task.workspaceId())) {
            throw new IllegalArgumentException(
                    "Workspace not found: "
                            + task.workspaceId());
        }

        if (tasks.containsKey(task.id())) {
            throw new IllegalArgumentException(
                    "Workspace task already exists: "
                            + task.id());
        }

        tasks.put(task.id(), task);

        return task;
    }

    public WorkspaceTask findById(String id) {

        return Optional.ofNullable(tasks.get(id))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Workspace task not found: "
                                        + id));
    }

    public List<WorkspaceTask> findAll() {
        return List.copyOf(tasks.values());
    }

    public boolean exists(String id) {
        return tasks.containsKey(id);
    }

    public void delete(String id) {

        if (!tasks.containsKey(id)) {
            throw new IllegalArgumentException(
                    "Workspace task not found: " + id);
        }

        tasks.remove(id);
    }

    public long count() {
        return tasks.size();
    }

    public EngineeringTask toEngineeringTask(
            String taskId) {

        WorkspaceTask workspaceTask =
                findById(taskId);

        return workspaceService.toEngineeringTask(
                workspaceTask
        );
    }

    public List<WorkspaceTask> findByWorkspaceId(
            String workspaceId) {

        return tasks.values()
                .stream()
                .filter(task ->
                        task.workspaceId().equals(workspaceId))
                .toList();
    }
}