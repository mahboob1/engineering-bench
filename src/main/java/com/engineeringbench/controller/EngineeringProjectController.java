package com.engineeringbench.controller;

import com.engineeringbench.model.EngineeringProject;
import com.engineeringbench.service.EngineeringProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class EngineeringProjectController {

    private final EngineeringProjectService projectService;

    public EngineeringProjectController(
            EngineeringProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public EngineeringProject create(
            @RequestBody EngineeringProject project) {

        return projectService.create(project);
    }

    @GetMapping("/{id}")
    public EngineeringProject findById(
            @PathVariable String id) {

        return projectService.findById(id);
    }

    @GetMapping
    public List<EngineeringProject> findAll() {
        return projectService.findAll();
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable String id) {

        projectService.delete(id);
    }
}