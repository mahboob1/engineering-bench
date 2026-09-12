package com.engineeringbench.model;

import java.util.List;

public record EngineeringProject(
        String id,
        String name,
        RepositoryReference repository,
        ProjectTechnology technology,
        List<ProjectCapability> capabilities
) {
    public EngineeringProject {
        if (capabilities == null) {
            capabilities = List.of();
        }
    }
}