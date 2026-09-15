package com.engineeringbench.service;

import com.engineeringbench.model.SearchResult;
import com.engineeringbench.model.WorkspaceTask;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryContextService {

    private final SemanticSearchService semanticSearchService;
    private final EngineeringWorkspaceService workspaceService;
    private final WorkspaceTaskService workspaceTaskService;

    public RepositoryContextService(
            SemanticSearchService semanticSearchService,
            EngineeringWorkspaceService workspaceService,
            WorkspaceTaskService workspaceTaskService) {
        this.semanticSearchService = semanticSearchService;
        this.workspaceService = workspaceService;
        this.workspaceTaskService = workspaceTaskService;
    }

    public String retrieve(
            String repository,
            String taskId) {
        WorkspaceTask workspaceTask =
                workspaceTaskService.findById(taskId);
        String collection =
                workspaceService.resolveCollection(
                        workspaceTask.workspaceId()
                );
        List<SearchResult> results =
                semanticSearchService.searchForAnalysis(
                        collection,
                        taskId,
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