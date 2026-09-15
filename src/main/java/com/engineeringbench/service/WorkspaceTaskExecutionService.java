package com.engineeringbench.service;

import com.engineeringbench.model.WorkspaceTaskResult;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceTaskExecutionService {

    private final WorkspaceTaskService workspaceTaskService;
    private final EngineeringAgentService engineeringAgentService;
    private final WorkspaceTaskResultService resultService;

    public WorkspaceTaskExecutionService(
            WorkspaceTaskService workspaceTaskService,
            EngineeringAgentService engineeringAgentService,
            WorkspaceTaskResultService resultService) {

        this.workspaceTaskService = workspaceTaskService;
        this.engineeringAgentService = engineeringAgentService;
        this.resultService = resultService;
    }

    public String execute(String taskId) {

        workspaceTaskService.markRunning(taskId);

        var engineeringTask =
                workspaceTaskService.toEngineeringTask(taskId);

        try {

            String result =
                    engineeringAgentService.execute(
                            engineeringTask,
                            taskId
                    );
            resultService.save(
                    new WorkspaceTaskResult(
                            taskId,
                            result
                    )
            );
            workspaceTaskService.markCompleted(taskId);

            return result;

        } catch (RuntimeException e) {

            workspaceTaskService.markFailed(taskId);

            throw e;
        }
    }
}