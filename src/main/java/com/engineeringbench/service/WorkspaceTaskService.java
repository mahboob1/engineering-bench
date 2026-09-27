package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringTask;
import com.engineeringbench.model.WorkspaceTask;
import com.engineeringbench.model.WorkspaceTaskStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkspaceTaskService {

    private final WorkspaceTaskRepository taskRepository;

    private final EngineeringWorkspaceService workspaceService;

    public WorkspaceTaskService(
            WorkspaceTaskRepository taskRepository,
            EngineeringWorkspaceService workspaceService) {

        this.taskRepository = taskRepository;
        this.workspaceService = workspaceService;
    }

    public WorkspaceTask create(
            WorkspaceTask task) {

        if (!workspaceService.exists(task.workspaceId())) {
            throw new IllegalArgumentException(
                    "Workspace not found: "
                            + task.workspaceId());
        }

        String id = task.id();

        if (id == null || id.isBlank()) {
            id = "task-" + java.util.UUID.randomUUID();
        }

        if (taskRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException(
                    "Workspace task already exists: "
                            + id);
        }

        WorkspaceTask created =
                new WorkspaceTask(
                        id,
                        task.workspaceId(),
                        task.task()
                );

        taskRepository.save(created);

        return created;
    }

    public WorkspaceTask findById(String id) {

        return taskRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Workspace task not found: "
                                        + id));
    }

    public List<WorkspaceTask> findAll() {

        return taskRepository.findAll();
    }

    public boolean exists(String id) {

        return taskRepository.findById(id).isPresent();
    }

    public void delete(String id) {

        if (taskRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException(
                    "Workspace task not found: " + id);
        }

        taskRepository.deleteById(id);
    }

    public long count() {

        return taskRepository.count();
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

        return taskRepository.findByWorkspaceId(
                workspaceId
        );
    }

    public WorkspaceTask markRunning(String id) {

        WorkspaceTask task = findById(id);

        WorkspaceTask updated =
                new WorkspaceTask(
                        task.id(),
                        task.workspaceId(),
                        task.task(),
                        WorkspaceTaskStatus.RUNNING
                );

        taskRepository.save(updated);

        return updated;
    }

    public WorkspaceTask markCompleted(String id) {

        WorkspaceTask task = findById(id);

        WorkspaceTask updated =
                new WorkspaceTask(
                        task.id(),
                        task.workspaceId(),
                        task.task(),
                        WorkspaceTaskStatus.COMPLETED
                );

        taskRepository.save(updated);

        return updated;
    }

    public WorkspaceTask markFailed(String id) {

        WorkspaceTask task = findById(id);

        WorkspaceTask updated =
                new WorkspaceTask(
                        task.id(),
                        task.workspaceId(),
                        task.task(),
                        WorkspaceTaskStatus.FAILED
                );

        taskRepository.save(updated);

        return updated;
    }
}