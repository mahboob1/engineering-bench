package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringProject;
import com.engineeringbench.model.EngineeringTask;
import com.engineeringbench.model.EngineeringWorkspace;
import com.engineeringbench.model.ProjectTechnology;
import com.engineeringbench.model.RepositoryReference;
import com.engineeringbench.model.WorkspaceTask;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceTaskServiceTest {

    @Test
    void shouldCreateAndRetrieveWorkspaceTask() {
        EngineeringProjectService projectService =
                createProjectService();

        EngineeringWorkspaceService workspaceService =
                createWorkspaceService(projectService);

        WorkspaceTaskService taskService =
                new WorkspaceTaskService(workspaceService);

        createWorkspace(
                projectService,
                workspaceService
        );

        WorkspaceTask task =
                new WorkspaceTask(
                        "task-001",
                        "workspace-001",
                        "Add customer search."
                );

        taskService.create(task);

        assertEquals(
                task,
                taskService.findById("task-001")
        );
    }

    @Test
    void shouldRejectTaskForUnknownWorkspace() {
        EngineeringProjectService projectService =
                createProjectService();

        EngineeringWorkspaceService workspaceService =
                createWorkspaceService(projectService);

        WorkspaceTaskService taskService =
                new WorkspaceTaskService(workspaceService);

        WorkspaceTask task =
                new WorkspaceTask(
                        "task-001",
                        "unknown-workspace",
                        "Add customer search."
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.create(task)
        );
    }

    @Test
    void shouldRejectDuplicateTask() {
        EngineeringProjectService projectService =
                createProjectService();

        EngineeringWorkspaceService workspaceService =
                createWorkspaceService(projectService);

        WorkspaceTaskService taskService =
                new WorkspaceTaskService(workspaceService);

        createWorkspace(
                projectService,
                workspaceService
        );

        WorkspaceTask task =
                new WorkspaceTask(
                        "task-001",
                        "workspace-001",
                        "Add customer search."
                );

        taskService.create(task);

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.create(task)
        );
    }

    @Test
    void shouldConvertWorkspaceTaskToEngineeringTask() {
        EngineeringProjectService projectService =
                createProjectService();

        EngineeringWorkspaceService workspaceService =
                createWorkspaceService(projectService);

        WorkspaceTaskService taskService =
                new WorkspaceTaskService(workspaceService);

        createWorkspace(
                projectService,
                workspaceService
        );

        WorkspaceTask task =
                new WorkspaceTask(
                        "task-001",
                        "workspace-001",
                        "Add customer search and write tests."
                );

        taskService.create(task);

        EngineeringTask engineeringTask =
                taskService.toEngineeringTask("task-001");

        assertEquals(
                "https://github.com/example/customer-service.git",
                engineeringTask.repository()
        );

        assertEquals(
                "feature/customer-search",
                engineeringTask.revision()
        );

        assertEquals(
                "Add customer search and write tests.",
                engineeringTask.task()
        );
    }

    @Test
    void shouldDeleteWorkspaceTask() {
        EngineeringProjectService projectService =
                createProjectService();

        EngineeringWorkspaceService workspaceService =
                createWorkspaceService(projectService);

        WorkspaceTaskService taskService =
                new WorkspaceTaskService(workspaceService);

        createWorkspace(
                projectService,
                workspaceService
        );

        WorkspaceTask task =
                new WorkspaceTask(
                        "task-001",
                        "workspace-001",
                        "Add customer search."
                );

        taskService.create(task);

        taskService.delete("task-001");

        assertFalse(
                taskService.exists("task-001")
        );
    }

    @Test
    void shouldReturnAllWorkspaceTasks() {
        EngineeringProjectService projectService =
                createProjectService();

        EngineeringWorkspaceService workspaceService =
                createWorkspaceService(projectService);

        WorkspaceTaskService taskService =
                new WorkspaceTaskService(workspaceService);

        createWorkspace(
                projectService,
                workspaceService
        );

        taskService.create(
                new WorkspaceTask(
                        "task-001",
                        "workspace-001",
                        "Add customer search."
                )
        );

        taskService.create(
                new WorkspaceTask(
                        "task-002",
                        "workspace-001",
                        "Add customer search tests."
                )
        );

        assertEquals(
                2,
                taskService.findAll().size()
        );
    }

    @Test
    void shouldReturnTaskCount() {
        EngineeringProjectService projectService =
                createProjectService();

        EngineeringWorkspaceService workspaceService =
                createWorkspaceService(projectService);

        WorkspaceTaskService taskService =
                new WorkspaceTaskService(workspaceService);

        createWorkspace(
                projectService,
                workspaceService
        );

        taskService.create(
                new WorkspaceTask(
                        "task-001",
                        "workspace-001",
                        "Add customer search."
                )
        );

        assertEquals(
                1,
                taskService.count()
        );
    }

    @Test
    void shouldFindWorkspaceTasksByWorkspaceId() {

        EngineeringProjectService projectService =
                createProjectService();

        EngineeringWorkspaceService workspaceService =
                createWorkspaceService(projectService);

        WorkspaceTaskService taskService =
                new WorkspaceTaskService(workspaceService);

        createWorkspace(
                projectService,
                workspaceService
        );

        // Create a second workspace for the task
        EngineeringProject secondProject =
                new EngineeringProject(
                        "order-service",
                        "Order Service",
                        new RepositoryReference(
                                "https://github.com/example/order-service.git",
                                "main"
                        ),
                        new ProjectTechnology(
                                "Java",
                                "Spring Boot",
                                "Gradle"
                        ),
                        List.of()
                );

        projectService.create(secondProject);

        workspaceService.create(
                new EngineeringWorkspace(
                        "workspace-002",
                        "order-service",
                        "feature/authentication"
                )
        );

        WorkspaceTask firstTask =
                new WorkspaceTask(
                        "task-001",
                        "workspace-001",
                        "Add customer search."
                );

        WorkspaceTask secondTask =
                new WorkspaceTask(
                        "task-002",
                        "workspace-001",
                        "Add pagination."
                );

        WorkspaceTask otherTask =
                new WorkspaceTask(
                        "task-003",
                        "workspace-002",
                        "Add authentication."
                );

        taskService.create(firstTask);
        taskService.create(secondTask);
        taskService.create(otherTask);

        List<WorkspaceTask> tasks =
                taskService.findByWorkspaceId("workspace-001");

        assertEquals(2, tasks.size());
        assertTrue(tasks.contains(firstTask));
        assertTrue(tasks.contains(secondTask));
        assertFalse(tasks.contains(otherTask));
    }

    private EngineeringProjectService createProjectService() {
        EngineeringProjectRepository repository =
                new InMemoryEngineeringProjectRepository();

        return new EngineeringProjectService(repository);
    }

    private EngineeringWorkspaceService createWorkspaceService(
            EngineeringProjectService projectService) {

        EngineeringWorkspaceRepository repository =
                new InMemoryEngineeringWorkspaceRepository();

        return new EngineeringWorkspaceService(
                repository,
                projectService
        );
    }

    private void createWorkspace(
            EngineeringProjectService projectService,
            EngineeringWorkspaceService workspaceService) {

        EngineeringProject project =
                new EngineeringProject(
                        "customer-service",
                        "Customer Service",
                        new RepositoryReference(
                                "https://github.com/example/customer-service.git",
                                "main"
                        ),
                        new ProjectTechnology(
                                "Java",
                                "Spring Boot",
                                "Gradle"
                        ),
                        List.of()
                );

        projectService.create(project);

        workspaceService.create(
                new EngineeringWorkspace(
                        "workspace-001",
                        "customer-service",
                        "feature/customer-search"
                )
        );
    }
}