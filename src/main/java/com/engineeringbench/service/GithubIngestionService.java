package com.engineeringbench.service;

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

    public void ingestRepository(
            String repoPath)
            throws Exception {

        try (Stream<Path> paths =
                     Files.walk(
                             Path.of(repoPath))) {

            paths.filter(Files::isRegularFile)
                    .filter(this::supportedFile)
                    .forEach(file -> {

                        try {

                            String content =
                                    Files.readString(file);

                            ingestionService.ingestText(
                                    file.getFileName()
                                            .toString(),
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
}