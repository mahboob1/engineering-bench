package com.engineeringbench.controller;

import com.engineeringbench.service.GithubRepositoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github/repositories")
public class GithubRepositoryController {

    private final GithubRepositoryService githubRepositoryService;

    public GithubRepositoryController(
            GithubRepositoryService githubRepositoryService) {
        this.githubRepositoryService = githubRepositoryService;
    }

    @PostMapping
    public ResponseEntity<GithubRepositoryService.GithubRepository>
    createRepository(
            @RequestBody CreateRepositoryRequest request)
            throws Exception {

        GithubRepositoryService.GithubRepository repository =
                githubRepositoryService.createRepository(
                        request.name(),
                        request.description()
                );

        return ResponseEntity.ok(repository);
    }

    public record CreateRepositoryRequest(
            String name,
            String description) {
    }
}