package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringProject;
import com.engineeringbench.model.ProjectCapability;
import com.engineeringbench.model.ProjectTechnology;
import com.engineeringbench.model.RepositoryReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EngineeringProjectProvisioningService {

    private final GithubRepositoryService githubRepositoryService;
    private final FargateSandboxService fargateSandboxService;
    private final EngineeringProjectService projectService;
    private final EngineeringWorkspaceService workspaceService;

    public EngineeringProjectProvisioningService(
            GithubRepositoryService githubRepositoryService,
            FargateSandboxService fargateSandboxService,
            EngineeringProjectService projectService,
            EngineeringWorkspaceService workspaceService) {

        this.githubRepositoryService = githubRepositoryService;
        this.fargateSandboxService = fargateSandboxService;
        this.projectService = projectService;
        this.workspaceService = workspaceService;
    }

    public EngineeringProject provision(
            String projectId,
            String projectName,
            String sourceRepositoryUrl,
            String sourceRevision,
            String workingRepositoryName,
            String description,
            String collection,
            ProjectTechnology technology,
            List<ProjectCapability> capabilities)
            throws Exception {

        GithubRepositoryService.GithubRepository workingRepository =
                githubRepositoryService.findRepository(
                        workingRepositoryName
                );

        if (workingRepository == null) {
            workingRepository =
                    githubRepositoryService.createRepository(
                            workingRepositoryName,
                            description
                    );

            fargateSandboxService.initializeWorkingRepository(
                    sourceRepositoryUrl,
                    workingRepository.cloneUrl()
            );
        }

        EngineeringProject project =
                new EngineeringProject(
                        projectId,
                        projectName,
                        new RepositoryReference(
                                sourceRepositoryUrl,
                                sourceRevision
                        ),
                        new RepositoryReference(
                                workingRepository.cloneUrl(),
                                sourceRevision
                        ),
                        collection,
                        technology,
                        capabilities
                );

        EngineeringProject createdProject =
                projectService.create(project);

        workspaceService.create(
                projectId,
                projectId + "-workspace",
                sourceRevision
        );

        return createdProject;
    }
}