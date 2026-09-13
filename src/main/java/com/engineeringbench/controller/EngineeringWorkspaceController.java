package com.engineeringbench.controller;

import com.engineeringbench.model.EngineeringWorkspace;
import com.engineeringbench.model.RepositoryReference;
import com.engineeringbench.service.EngineeringWorkspaceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
public class EngineeringWorkspaceController {

    private final EngineeringWorkspaceService workspaceService;

    public EngineeringWorkspaceController(
            EngineeringWorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public EngineeringWorkspace create(
            @RequestBody EngineeringWorkspace workspace) {

        return workspaceService.create(workspace);
    }

    @GetMapping("/{id}")
    public EngineeringWorkspace findById(
            @PathVariable String id) {

        return workspaceService.findById(id);
    }

    @GetMapping
    public List<EngineeringWorkspace> findAll() {
        return workspaceService.findAll();
    }

    @GetMapping("/{id}/exists")
    public boolean exists(
            @PathVariable String id) {

        return workspaceService.exists(id);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable String id) {

        workspaceService.delete(id);
    }

    @GetMapping("/{id}/repository")
    public RepositoryReference getRepository(
            @PathVariable String id) {

        return workspaceService.resolveRepository(id);
    }

    @PostMapping("/from-project")
    public EngineeringWorkspace createFromProject(
            @RequestParam String projectId,
            @RequestParam String workspaceId,
            @RequestParam String revision) {

        return workspaceService.create(
                projectId,
                workspaceId,
                revision
        );
    }

    @GetMapping("/count")
    public long count() {
        return workspaceService.count();
    }
}