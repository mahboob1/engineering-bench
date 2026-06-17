package com.engineeringbench;

import com.engineeringbench.service.GithubIngestionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RepositoryController {

    private final GithubIngestionService service;

    public RepositoryController(
            GithubIngestionService service) {

        this.service = service;
    }

    @PostMapping("/repo")
    public String ingestRepo(
            @RequestParam String path)
            throws Exception {

        service.ingestRepository(
                path);

        return "repository indexed";
    }
}