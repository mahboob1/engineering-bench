package com.engineeringbench.controller;

import com.engineeringbench.model.EngineeringProject;
import com.engineeringbench.model.ProjectCapability;
import com.engineeringbench.model.ProjectTechnology;
import com.engineeringbench.service.EngineeringProjectProvisioningService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class EngineeringProjectProvisioningController {

    private final EngineeringProjectProvisioningService provisioningService;

    public EngineeringProjectProvisioningController(
            EngineeringProjectProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @PostMapping("/provision")
    public EngineeringProject provision(
            @RequestBody ProvisionProjectRequest request)
            throws Exception {

        return provisioningService.provision(
                request.projectId(),
                request.projectName(),
                request.sourceRepositoryUrl(),
                request.sourceRevision(),
                request.workingRepositoryName(),
                request.description(),
                request.collection(),
                request.technology(),
                request.capabilities()
        );
    }

    public record ProvisionProjectRequest(
            String projectId,
            String projectName,
            String sourceRepositoryUrl,
            String sourceRevision,
            String workingRepositoryName,
            String description,
            String collection,
            ProjectTechnology technology,
            List<ProjectCapability> capabilities
    ) {
    }
}