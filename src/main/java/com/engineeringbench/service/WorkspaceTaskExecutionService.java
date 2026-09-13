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

        workspaceTaskService.markRunning(taskId);

        var engineeringTask =
                workspaceTaskService.toEngineeringTask(taskId);

        try {

            String result =
                    engineeringAgentService.execute(
                            engineeringTask
                    );

            workspaceTaskService.markCompleted(taskId);

            return result;

        } catch (RuntimeException e) {

            workspaceTaskService.markFailed(taskId);

            throw e;
        }
    }
}