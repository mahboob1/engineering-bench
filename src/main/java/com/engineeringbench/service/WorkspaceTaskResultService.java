package com.engineeringbench.service;

import com.engineeringbench.model.WorkspaceTaskResult;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkspaceTaskResultService {

    private final Map<String, WorkspaceTaskResult> results =
            new ConcurrentHashMap<>();

    public WorkspaceTaskResult save(
            WorkspaceTaskResult result) {

        results.put(result.taskId(), result);

        return result;
    }

    public WorkspaceTaskResult findByTaskId(
            String taskId) {

        return Optional.ofNullable(results.get(taskId))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Workspace task result not found: "
                                        + taskId));
    }
}