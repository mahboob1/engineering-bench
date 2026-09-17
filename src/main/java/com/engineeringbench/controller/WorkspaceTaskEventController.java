package com.engineeringbench.controller;

import com.engineeringbench.model.ExecutionEvent;
import com.engineeringbench.service.ExecutionEventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspace-tasks")
public class WorkspaceTaskEventController {

    private final ExecutionEventService eventService;

    public WorkspaceTaskEventController(
            ExecutionEventService eventService) {

        this.eventService = eventService;
    }

    @GetMapping("/{taskId}/events")
    public List<ExecutionEvent> getEvents(
            @PathVariable String taskId) {

        return eventService.findByTaskId(taskId);
    }
}