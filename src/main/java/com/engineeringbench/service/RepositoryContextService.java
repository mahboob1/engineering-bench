package com.engineeringbench.service;

import com.engineeringbench.model.SearchResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryContextService {

    private final SemanticSearchService semanticSearchService;

    public RepositoryContextService(
            SemanticSearchService semanticSearchService) {

        this.semanticSearchService =
                semanticSearchService;
    }

    public String retrieve(
            String repository,
            String question) {

        List<SearchResult> results =
                semanticSearchService.searchForAnalysis(
                        "engineering_docs",
                        question,
                        repository
                );

        if (results.isEmpty()) {
            return "No repository evidence was retrieved.";
        }

        StringBuilder context =
                new StringBuilder();

        for (SearchResult result : results) {

            context.append("""
                    ---
                    Source: %s
                    Repository: %s
                    Relevance Score: %s

                    %s

                    """.formatted(
                    result.source(),
                    result.repository(),
                    result.score(),
                    result.content()
            ));
        }

        return context.toString();
    }
}