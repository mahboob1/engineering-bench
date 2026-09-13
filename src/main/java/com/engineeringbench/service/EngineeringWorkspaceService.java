package com.engineeringbench.service;

import com.engineeringbench.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EngineeringWorkspaceService {

    private final EngineeringWorkspaceRepository workspaceRepository;
    private final EngineeringProjectService projectService;

    public EngineeringWorkspaceService(
            EngineeringWorkspaceRepository workspaceRepository,
            EngineeringProjectService projectService) {

        this.workspaceRepository = workspaceRepository;
        this.projectService = projectService;
    }
    public EngineeringTask toEngineeringTask(
            WorkspaceTask workspaceTask) {

        RepositoryReference repository =
                resolveRepository(workspaceTask.workspaceId());

        return new EngineeringTask(
                repository.url(),
                repository.revision(),
                workspaceTask.task()
        );
    }


    public EngineeringWorkspace create(
            EngineeringWorkspace workspace) {

        if (!projectService.exists(workspace.projectId())) {
            throw new IllegalArgumentException(
                    "Project not found: "
                            + workspace.projectId());
        }

        if (workspaceRepository.findById(workspace.id()).isPresent()) {
            throw new IllegalArgumentException(
                    "Workspace already exists: "
                            + workspace.id());
        }

        return workspaceRepository.save(workspace);
    }

    public EngineeringWorkspace findById(
            String id) {

        return workspaceRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Workspace not found: " + id));
    }

    public List<EngineeringWorkspace> findAll() {
        return workspaceRepository.findAll();
    }

    public boolean exists(String id) {
        return workspaceRepository.findById(id).isPresent();
    }

    public void delete(String id) {

        if (workspaceRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException(
                    "Workspace not found: " + id);
        }

        workspaceRepository.deleteById(id);
    }

    public RepositoryReference getRepository(
            String workspaceId) {

        EngineeringWorkspace workspace =
                findById(workspaceId);

        EngineeringProject project =
                projectService.findById(workspace.projectId());

        return project.repository();
    }

    public RepositoryReference resolveRepository(
            String workspaceId) {

        EngineeringWorkspace workspace =
                findById(workspaceId);

        EngineeringProject project =
                projectService.findById(workspace.projectId());

        return new RepositoryReference(
                project.repository().url(),
                workspace.revision()
        );
    }

    public EngineeringWorkspace create(
            String projectId,
            String workspaceId,
            String revision) {

        if (!projectService.exists(projectId)) {
            throw new IllegalArgumentException(
                    "Project not found: " + projectId);
        }

        if (workspaceRepository.findById(workspaceId).isPresent()) {
            throw new IllegalArgumentException(
                    "Workspace already exists: " + workspaceId);
        }

        EngineeringWorkspace workspace =
                new EngineeringWorkspace(
                        workspaceId,
                        projectId,
                        revision
                );

        return workspaceRepository.save(workspace);
    }

    public long count() {
        return workspaceRepository.count();
    }
}