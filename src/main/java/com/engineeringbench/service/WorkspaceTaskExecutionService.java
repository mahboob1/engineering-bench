package com.engineeringbench.service;

import org.springframework.stereotype.Service;

@Service
public class WorkspaceTaskExecutionService {

    private final WorkspaceTaskService workspaceTaskService;
    private final EngineeringAgentService engineeringAgentService;

    public WorkspaceTaskExecutionService(
            WorkspaceTaskService workspaceTaskService,
            EngineeringAgentService engineeringAgentService) {

        this.workspaceTaskService = workspaceTaskService;
        this.engineeringAgentService = engineeringAgentService;
    }

    public String execute(String taskId) {

        var engineeringTask =
                workspaceTaskService.toEngineeringTask(taskId);

        return engineeringAgentService.execute(
                engineeringTask
        );
    }
}