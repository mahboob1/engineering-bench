package com.engineeringbench.service;

import com.engineeringbench.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EngineeringWorkspaceServiceTest {

    @Test
    void shouldCreateRetrieveAndDeleteWorkspace() {

        EngineeringProjectRepository projectRepository =
                new InMemoryEngineeringProjectRepository();

        EngineeringProjectService projectService =
                new EngineeringProjectService(projectRepository);

        EngineeringWorkspaceRepository workspaceRepository =
                new InMemoryEngineeringWorkspaceRepository();

        EngineeringWorkspaceService workspaceService =
                new EngineeringWorkspaceService(
                        workspaceRepository,
                        projectService);

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
                        List.of(
                                new ProjectCapability(
                                        "DATABASE",
                                        "PostgreSQL"
                                )
                        )
                );

        projectService.create(project);

        EngineeringWorkspace workspace =
                new EngineeringWorkspace(
                        "workspace-001",
                        "customer-service",
                        "main"
                );

        workspaceService.create(workspace);

        assertEquals(
                workspace,
                workspaceService.findById("workspace-001")
        );

        assertTrue(
                workspaceService.exists("workspace-001")
        );

        workspaceService.delete("workspace-001");

        assertFalse(
                workspaceService.exists("workspace-001")
        );
    }

    @Test
    void shouldResolveRepositoryUsingWorkspaceRevision() {

        EngineeringProjectRepository projectRepository =
                new InMemoryEngineeringProjectRepository();

        EngineeringProjectService projectService =
                new EngineeringProjectService(projectRepository);

        EngineeringWorkspaceRepository workspaceRepository =
                new InMemoryEngineeringWorkspaceRepository();

        EngineeringWorkspaceService workspaceService =
                new EngineeringWorkspaceService(
                        workspaceRepository,
                        projectService);

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

        EngineeringWorkspace workspace =
                new EngineeringWorkspace(
                        "workspace-001",
                        "customer-service",
                        "feature/customer-search"
                );

        workspaceService.create(workspace);

        RepositoryReference resolved =
                workspaceService.resolveRepository(
                        "workspace-001"
                );

        assertEquals(
                "https://github.com/example/customer-service.git",
                resolved.url()
        );

        assertEquals(
                "feature/customer-search",
                resolved.revision()
        );
    }

    @Test
    void shouldCreateWorkspaceFromProject() {

        EngineeringProjectRepository projectRepository =
                new InMemoryEngineeringProjectRepository();

        EngineeringProjectService projectService =
                new EngineeringProjectService(projectRepository);

        EngineeringWorkspaceRepository workspaceRepository =
                new InMemoryEngineeringWorkspaceRepository();

        EngineeringWorkspaceService workspaceService =
                new EngineeringWorkspaceService(
                        workspaceRepository,
                        projectService);

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

        EngineeringWorkspace workspace =
                workspaceService.create(
                        "customer-service",
                        "workspace-001",
                        "feature/customer-search"
                );

        assertEquals(
                "workspace-001",
                workspace.id()
        );

        assertEquals(
                "customer-service",
                workspace.projectId()
        );

        assertEquals(
                "feature/customer-search",
                workspace.revision()
        );
    }

    @Test
    void shouldRejectWorkspaceForUnknownProject() {

        EngineeringProjectRepository projectRepository =
                new InMemoryEngineeringProjectRepository();

        EngineeringProjectService projectService =
                new EngineeringProjectService(projectRepository);

        EngineeringWorkspaceRepository workspaceRepository =
                new InMemoryEngineeringWorkspaceRepository();

        EngineeringWorkspaceService workspaceService =
                new EngineeringWorkspaceService(
                        workspaceRepository,
                        projectService);

        assertThrows(
                IllegalArgumentException.class,
                () -> workspaceService.create(
                        "unknown-project",
                        "workspace-001",
                        "main"
                )
        );
    }

    @Test
    void shouldRejectDuplicateWorkspace() {

        EngineeringProjectRepository projectRepository =
                new InMemoryEngineeringProjectRepository();

        EngineeringProjectService projectService =
                new EngineeringProjectService(projectRepository);

        EngineeringWorkspaceRepository workspaceRepository =
                new InMemoryEngineeringWorkspaceRepository();

        EngineeringWorkspaceService workspaceService =
                new EngineeringWorkspaceService(
                        workspaceRepository,
                        projectService);

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
                "customer-service",
                "workspace-001",
                "main"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> workspaceService.create(
                        "customer-service",
                        "workspace-001",
                        "feature/test"
                )
        );
    }

    @Test
    void shouldRejectDeletingUnknownWorkspace() {

        EngineeringProjectRepository projectRepository =
                new InMemoryEngineeringProjectRepository();

        EngineeringProjectService projectService =
                new EngineeringProjectService(projectRepository);

        EngineeringWorkspaceRepository workspaceRepository =
                new InMemoryEngineeringWorkspaceRepository();

        EngineeringWorkspaceService workspaceService =
                new EngineeringWorkspaceService(
                        workspaceRepository,
                        projectService);

        assertThrows(
                IllegalArgumentException.class,
                () -> workspaceService.delete("unknown-workspace")
        );
    }

    @Test
    void shouldConvertWorkspaceTaskToEngineeringTask() {
        EngineeringProjectRepository projectRepository =
                new InMemoryEngineeringProjectRepository();

        EngineeringProjectService projectService =
                new EngineeringProjectService(projectRepository);

        EngineeringWorkspaceRepository workspaceRepository =
                new InMemoryEngineeringWorkspaceRepository();

        EngineeringWorkspaceService workspaceService =
                new EngineeringWorkspaceService(
                        workspaceRepository,
                        projectService);

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

        EngineeringWorkspace workspace =
                new EngineeringWorkspace(
                        "workspace-001",
                        "customer-service",
                        "feature/customer-search"
                );

        workspaceService.create(workspace);

        WorkspaceTask workspaceTask =
                new WorkspaceTask(
                        "task-001",
                        "workspace-001",
                        "Add customer search by name and write tests."
                );

        EngineeringTask engineeringTask =
                workspaceService.toEngineeringTask(
                        workspaceTask
                );

        assertEquals(
                "https://github.com/example/customer-service.git",
                engineeringTask.repository()
        );

        assertEquals(
                "feature/customer-search",
                engineeringTask.revision()
        );

        assertEquals(
                "Add customer search by name and write tests.",
                engineeringTask.task()
        );
    }
}