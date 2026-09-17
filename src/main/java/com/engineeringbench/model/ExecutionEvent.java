package com.engineeringbench.model;

import java.time.Instant;

public record ExecutionEvent(
        String id,
        String taskId,
        int iteration,
        String type,
        String message,
        Instant timestamp
) {
}