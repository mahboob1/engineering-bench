package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringProject;
import com.engineeringbench.model.ProjectCapability;
import com.engineeringbench.model.ProjectTechnology;
import com.engineeringbench.model.RepositoryReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EngineeringProjectProvisioningServiceTest {

    @Test
    void shouldCreateWorkingRepositoryInitializeItAndPersistProject()
            throws Exception {

        GithubRepositoryService githubRepositoryService =
                mock(GithubRepositoryService.class);

        FargateSandboxService fargateSandboxService =
                mock(FargateSandboxService.class);

        EngineeringProjectService projectService =
                mock(EngineeringProjectService.class);

        EngineeringWorkspaceService workspaceService =
                mock(EngineeringWorkspaceService.class);

        EngineeringProjectProvisioningService service =
                new EngineeringProjectProvisioningService(
                        githubRepositoryService,
                        fargateSandboxService,
                        projectService,
                        workspaceService
                );

        GithubRepositoryService.GithubRepository workingRepository =
                new GithubRepositoryService.GithubRepository(
                        "engineering-bench-petclinic",
                        "mahboob1/engineering-bench-petclinic",
                        "https://github.com/mahboob1/engineering-bench-petclinic",
                        "https://github.com/mahboob1/engineering-bench-petclinic.git",
                        false
                );

        when(githubRepositoryService.findRepository(
                "engineering-bench-petclinic"
        )).thenReturn(null);

        when(githubRepositoryService.createRepository(
                "engineering-bench-petclinic",
                "Working repository created by Engineering Bench"
        )).thenReturn(workingRepository);

        EngineeringProject expectedProject =
                new EngineeringProject(
                        "petclinic",
                        "Petclinic",
                        new RepositoryReference(
                                "https://github.com/spring-projects/spring-petclinic.git",
                                "main"
                        ),
                        new RepositoryReference(
                                "https://github.com/mahboob1/engineering-bench-petclinic.git",
                                "main"
                        ),
                        "engineering_docs",
                        new ProjectTechnology(
                                "Java",
                                "Spring Boot",
                                "Gradle"
                        ),
                        List.of(
                                new ProjectCapability(
                                        "repository",
                                        "source-analysis"
                                )
                        )
                );

        when(projectService.create(any(EngineeringProject.class)))
                .thenReturn(expectedProject);

        EngineeringProject result =
                service.provision(
                        "petclinic",
                        "Petclinic",
                        "https://github.com/spring-projects/spring-petclinic.git",
                        "main",
                        "engineering-bench-petclinic",
                        "Working repository created by Engineering Bench",
                        "engineering_docs",
                        expectedProject.technology(),
                        expectedProject.capabilities()
                );

        assertNotNull(result);
        assertEquals(
                "https://github.com/mahboob1/engineering-bench-petclinic.git",
                result.workingRepository().url()
        );

        verify(githubRepositoryService).findRepository(
                "engineering-bench-petclinic"
        );

        verify(githubRepositoryService)
                .createRepository(
                        "engineering-bench-petclinic",
                        "Working repository created by Engineering Bench"
                );

        verify(fargateSandboxService)
                .initializeWorkingRepository(
                        "https://github.com/spring-projects/spring-petclinic.git",
                        "https://github.com/mahboob1/engineering-bench-petclinic.git"
                );

        verify(projectService)
                .create(any(EngineeringProject.class));

        verify(workspaceService).create(
                "petclinic",
                "petclinic-workspace",
                "main"
        );
    }

    @Test
    void shouldReuseExistingWorkingRepository() throws Exception {

        GithubRepositoryService githubRepositoryService =
                mock(GithubRepositoryService.class);

        FargateSandboxService fargateSandboxService =
                mock(FargateSandboxService.class);

        EngineeringProjectService projectService =
                mock(EngineeringProjectService.class);

        EngineeringWorkspaceService workspaceService =
                mock(EngineeringWorkspaceService.class);

        EngineeringProjectProvisioningService service =
                new EngineeringProjectProvisioningService(
                        githubRepositoryService,
                        fargateSandboxService,
                        projectService,
                        workspaceService
                );


        GithubRepositoryService.GithubRepository existingRepository =
                new GithubRepositoryService.GithubRepository(
                        "engineering-bench-petclinic",
                        "mahboob1/engineering-bench-petclinic",
                        "https://github.com/mahboob1/engineering-bench-petclinic",
                        "https://github.com/mahboob1/engineering-bench-petclinic.git",
                        false
                );

        EngineeringProject expectedProject =
                new EngineeringProject(
                        "petclinic",
                        "Petclinic",
                        new RepositoryReference(
                                "https://github.com/spring-projects/spring-petclinic.git",
                                "main"
                        ),
                        new RepositoryReference(
                                existingRepository.cloneUrl(),
                                "main"
                        ),
                        "engineering_docs",
                        new ProjectTechnology(
                                "Java",
                                "Spring Boot",
                                "Gradle"
                        ),
                        List.of(
                                new ProjectCapability(
                                        "repository",
                                        "source-analysis"
                                )
                        )
                );

        when(githubRepositoryService.findRepository(
                "engineering-bench-petclinic"
        )).thenReturn(existingRepository);

        when(projectService.create(any(EngineeringProject.class)))
                .thenReturn(expectedProject);

        EngineeringProject result =
                service.provision(
                        "petclinic",
                        "Petclinic",
                        "https://github.com/spring-projects/spring-petclinic.git",
                        "main",
                        "engineering-bench-petclinic",
                        "Working repository created by Engineering Bench",
                        "engineering_docs",
                        expectedProject.technology(),
                        expectedProject.capabilities()
                );

        assertNotNull(result);
        assertEquals(
                "https://github.com/mahboob1/engineering-bench-petclinic.git",
                result.workingRepository().url()
        );

        verify(githubRepositoryService).findRepository(
                "engineering-bench-petclinic"
        );

        verify(githubRepositoryService, never()).createRepository(
                anyString(),
                anyString()
        );

        verify(fargateSandboxService, never())
                .initializeWorkingRepository(
                        anyString(),
                        anyString()
                );

        verify(projectService).create(
                any(EngineeringProject.class)
        );

        verify(workspaceService).create(
                "petclinic",
                "petclinic-workspace",
                "main"
        );
    }
}