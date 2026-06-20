package com.engineeringbench.model;

public record SearchResult(
        String content,
        String source,
        String repository,
        double score
) {}