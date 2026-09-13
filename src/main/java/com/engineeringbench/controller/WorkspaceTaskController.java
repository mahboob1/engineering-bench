package com.engineeringbench.controller;

import com.engineeringbench.model.EngineeringTask;
import com.engineeringbench.model.WorkspaceTask;
import com.engineeringbench.service.WorkspaceTaskExecutionService;
import com.engineeringbench.service.WorkspaceTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspace-tasks")
public class WorkspaceTaskController {

    private final WorkspaceTaskService taskService;
    private final WorkspaceTaskExecutionService executionService;

    public WorkspaceTaskController(
            WorkspaceTaskService taskService,
            WorkspaceTaskExecutionService executionService) {
        this.taskService = taskService;
        this.executionService = executionService;
    }

    @PostMapping
    public WorkspaceTask create(
            @RequestBody WorkspaceTask task) {
        return taskService.create(task);
    }

    @GetMapping("/{id}")
    public WorkspaceTask findById(
            @PathVariable String id) {
        return taskService.findById(id);
    }

    @GetMapping
    public List<WorkspaceTask> findAll() {
        return taskService.findAll();
    }

    @GetMapping("/{id}/exists")
    public boolean exists(
            @PathVariable String id) {
        return taskService.exists(id);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable String id) {
        taskService.delete(id);
    }

    @GetMapping("/{id}/engineering-task")
    public EngineeringTask toEngineeringTask(
            @PathVariable String id) {
        return taskService.toEngineeringTask(id);
    }

    @GetMapping("/count")
    public long count() {
        return taskService.count();
    }

    @GetMapping("/workspace/{workspaceId}")
    public List<WorkspaceTask> findByWorkspaceId(
            @PathVariable String workspaceId) {
        return taskService.findByWorkspaceId(workspaceId);
    }

    @PostMapping("/{id}/execute")
    public String execute(
            @PathVariable String id) {

        return executionService.execute(id);
    }
}