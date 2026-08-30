package com.engineeringbench.mcp;

import com.engineeringbench.model.SearchResult;
import com.engineeringbench.service.SemanticSearchService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EngineeringMcpTools {

    private final SemanticSearchService semanticSearchService;

    public EngineeringMcpTools(
            SemanticSearchService semanticSearchService) {

        this.semanticSearchService = semanticSearchService;
    }

    @Tool(
            name = "searchEngineeringDocs",
            description = """
                    Search engineering documentation using semantic similarity.
                    Returns the most relevant documentation chunks for a question.
                    Optionally restrict the search to a specific repository.
                    """
    )
    public List<SearchResult> searchEngineeringDocs(

            @ToolParam(
                    description = "The engineering question or search query",
                    required = true
            )
            String query,

            @ToolParam(
                    description = """
                            Optional repository name used to restrict the search.
                            Leave empty to search across all repositories.
                            """,
                    required = false
            )
            String repository) {

        return semanticSearchService.search(
                "engineering_docs",
                query,
                repository
        );
    }
}