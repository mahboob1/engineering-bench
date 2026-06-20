package com.engineeringbench.service;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public class GithubIngestionService {

    private final IngestionService ingestionService;

    public GithubIngestionService(
            IngestionService ingestionService) {

        this.ingestionService = ingestionService;
    }

    // For GitHub repositories
    public void ingestGithubUrl(
            String repoUrl)
            throws Exception {

        Path tempDir =
                Files.createTempDirectory(
                        "github-repo");

        Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(
                        tempDir.toFile())
                .call();

        ingestRepository(
                tempDir.toString());
    }

    // For local repositories
    public void ingestRepository(
            String repoPath)
            throws Exception {

        Path repoRoot =
                Path.of(repoPath);

        try (Stream<Path> paths =
                     Files.walk(
                             Path.of(repoPath))) {

            paths.filter(Files::isRegularFile)
                    .filter(this::supportedFile)
                    .forEach(file -> {

                        try {
                            String repository =
                                    repoNameFromUrl(repoPath);

                            String content =
                                    Files.readString(file);

                            String source =
                                    repoRoot
                                            .relativize(file)
                                            .toString();


                            ingestionService.ingestText(
                                    repository,
                                    source,
                                    content
                            );

                            System.out.println(
                                    "Indexed: "
                                            + file);

                        } catch (Exception e) {

                            throw new RuntimeException(
                                    e);
                        }
                    });
        }
    }

    private boolean supportedFile(
            Path path) {

        String name =
                path.toString()
                        .toLowerCase();

        return name.endsWith(".java")
                || name.endsWith(".yml")
                || name.endsWith(".yaml")
                || name.endsWith(".properties")
                || name.endsWith(".md")
                || name.endsWith(".gradle");
    }

    private String repoNameFromUrl(String repoUrl) {

        String repoName =
                repoUrl.substring(
                        repoUrl.lastIndexOf('/') + 1);

        if (repoName.endsWith(".git")) {
            repoName =
                    repoName.substring(
                            0,
                            repoName.length() - 4);
        }

        return repoName;
    }
}