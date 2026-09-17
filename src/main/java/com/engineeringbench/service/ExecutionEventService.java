package com.engineeringbench.service;

import com.engineeringbench.model.ExecutionEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ExecutionEventService {

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<ExecutionEvent>> events =
            new ConcurrentHashMap<>();

    public void clear(String taskId) {
        events.remove(taskId);
    }

    public ExecutionEvent add(
            String taskId,
            int iteration,
            String type,
            String message) {

        ExecutionEvent event =
                new ExecutionEvent(
                        UUID.randomUUID().toString(),
                        taskId,
                        iteration,
                        type,
                        message,
                        Instant.now()
                );

        events.computeIfAbsent(
                taskId,
                key -> new CopyOnWriteArrayList<>()
        ).add(event);

        return event;
    }

    public List<ExecutionEvent> findByTaskId(String taskId) {

        return List.copyOf(
                events.getOrDefault(
                        taskId,
                        new CopyOnWriteArrayList<>()
                )
        );
    }
}