package com.engineeringbench.model;

public record AnalysisCategory(
        String name,
        String query,
        int maxResults
) {}