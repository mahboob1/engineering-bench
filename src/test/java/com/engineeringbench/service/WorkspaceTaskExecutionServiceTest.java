package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringTask;
import com.engineeringbench.model.WorkspaceTaskResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class WorkspaceTaskExecutionServiceTest {

    @Mock
    private WorkspaceTaskResultService workspaceTaskResultService;

    @Test
    void shouldExecuteWorkspaceTaskThroughEngineeringAgent() {

        WorkspaceTaskService workspaceTaskService =
                mock(WorkspaceTaskService.class);

        EngineeringAgentService agentService =
                mock(EngineeringAgentService.class);

        WorkspaceTaskResultService resultService =
                mock(WorkspaceTaskResultService.class);

        WorkspaceTaskExecutionService executionService =
                new WorkspaceTaskExecutionService(
                        workspaceTaskService,
                        agentService,
                        resultService
                );

        EngineeringTask engineeringTask =
                new EngineeringTask(
                        "https://github.com/example/customer-service.git",
                        "feature/customer-search",
                        "Add customer search."
                );

        when(workspaceTaskService.toEngineeringTask("task-001"))
                .thenReturn(engineeringTask);

        when(agentService.execute(engineeringTask))
                .thenReturn("execution-result");

        String result =
                executionService.execute("task-001");

        assertEquals(
                "execution-result",
                result
        );

        verify(workspaceTaskService)
                .toEngineeringTask("task-001");

        verify(agentService)
                .execute(engineeringTask);
    }

    @Test
    void shouldMarkTaskCompletedAfterSuccessfulExecution() {

        WorkspaceTaskService workspaceTaskService =
                mock(WorkspaceTaskService.class);

        EngineeringAgentService agentService =
                mock(EngineeringAgentService.class);

        WorkspaceTaskResultService resultService =
                mock(WorkspaceTaskResultService.class);

        WorkspaceTaskExecutionService executionService =
                new WorkspaceTaskExecutionService(
                        workspaceTaskService,
                        agentService,
                        resultService
                );

        EngineeringTask engineeringTask =
                new EngineeringTask(
                        "https://github.com/example/customer-service.git",
                        "feature/customer-search",
                        "Add customer search."
                );

        when(workspaceTaskService.toEngineeringTask("task-001"))
                .thenReturn(engineeringTask);

        when(agentService.execute(engineeringTask))
                .thenReturn("execution-result");

        String result =
                executionService.execute("task-001");

        assertEquals(
                "execution-result",
                result
        );

        verify(workspaceTaskService)
                .markRunning("task-001");

        verify(workspaceTaskService)
                .markCompleted("task-001");
    }

    @Test
    void shouldMarkTaskFailedWhenExecutionThrowsException() {

        WorkspaceTaskService workspaceTaskService =
                mock(WorkspaceTaskService.class);

        EngineeringAgentService agentService =
                mock(EngineeringAgentService.class);

        WorkspaceTaskResultService resultService =
                mock(WorkspaceTaskResultService.class);

        WorkspaceTaskExecutionService executionService =
                new WorkspaceTaskExecutionService(
                        workspaceTaskService,
                        agentService,
                        resultService
                );

        EngineeringTask engineeringTask =
                new EngineeringTask(
                        "https://github.com/example/customer-service.git",
                        "feature/customer-search",
                        "Add customer search."
                );

        when(workspaceTaskService.toEngineeringTask("task-001"))
                .thenReturn(engineeringTask);

        when(agentService.execute(engineeringTask))
                .thenThrow(new RuntimeException("Execution failed"));

        assertThrows(
                RuntimeException.class,
                () -> executionService.execute("task-001")
        );

        verify(workspaceTaskService)
                .markRunning("task-001");

        verify(workspaceTaskService)
                .markFailed("task-001");
    }

    @Test
    void shouldStoreExecutionResultAfterSuccessfulExecution() {

        WorkspaceTaskService workspaceTaskService =
                mock(WorkspaceTaskService.class);

        EngineeringAgentService agentService =
                mock(EngineeringAgentService.class);

        WorkspaceTaskResultService resultService =
                mock(WorkspaceTaskResultService.class);

        WorkspaceTaskExecutionService executionService =
                new WorkspaceTaskExecutionService(
                        workspaceTaskService,
                        agentService,
                        resultService
                );

        EngineeringTask engineeringTask =
                new EngineeringTask(
                        "https://github.com/example/customer-service.git",
                        "feature/customer-search",
                        "Add customer search."
                );

        when(workspaceTaskService.toEngineeringTask("task-001"))
                .thenReturn(engineeringTask);

        when(agentService.execute(engineeringTask))
                .thenReturn("execution-result");

        executionService.execute("task-001");

        verify(resultService)
                .save(
                        new WorkspaceTaskResult(
                                "task-001",
                                "execution-result"
                        )
                );
    }
}