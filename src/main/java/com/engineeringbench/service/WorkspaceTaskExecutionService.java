package com.engineeringbench.service;

import com.engineeringbench.model.WorkspaceTaskResult;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceTaskExecutionService {

    private final WorkspaceTaskService workspaceTaskService;
    private final EngineeringAgentService engineeringAgentService;
    private final WorkspaceTaskResultService resultService;
    private final ExecutionEventService eventService;

    public WorkspaceTaskExecutionService(
            WorkspaceTaskService workspaceTaskService,
            EngineeringAgentService engineeringAgentService,
            WorkspaceTaskResultService resultService,
            ExecutionEventService eventService) {

        this.workspaceTaskService = workspaceTaskService;
        this.engineeringAgentService = engineeringAgentService;
        this.resultService = resultService;
        this.eventService = eventService;
    }

    public String execute(String taskId) {

        /*
         * Start a fresh event stream for this execution.
         */
        eventService.clear(taskId);

        eventService.add(
                taskId,
                0,
                "TASK_STARTED",
                "Engineering task execution started."
        );

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

            eventService.add(
                    taskId,
                    0,
                    "TASK_COMPLETED",
                    "Engineering task completed successfully."
            );

            return result;

        } catch (RuntimeException e) {

            workspaceTaskService.markFailed(taskId);

            eventService.add(
                    taskId,
                    0,
                    "TASK_FAILED",
                    "Engineering task execution failed: "
                            + e.getMessage()
            );

            throw e;
        }
    }
}