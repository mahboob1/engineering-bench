package com.engineeringbench.service;

import com.engineeringbench.model.WorkspaceTask;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryWorkspaceTaskRepository
        implements WorkspaceTaskRepository {

    private final Map<String, WorkspaceTask> tasks =
            new ConcurrentHashMap<>();

    @Override
    public WorkspaceTask save(
            WorkspaceTask task) {

        tasks.put(task.id(), task);
        return task;
    }

    @Override
    public Optional<WorkspaceTask> findById(
            String id) {

        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<WorkspaceTask> findAll() {

        return List.copyOf(tasks.values());
    }

    @Override
    public List<WorkspaceTask> findByWorkspaceId(
            String workspaceId) {

        return tasks.values()
                .stream()
                .filter(task ->
                        task.workspaceId().equals(workspaceId))
                .toList();
    }

    @Override
    public void deleteById(String id) {

        tasks.remove(id);
    }

    @Override
    public long count() {

        return tasks.size();
    }
}