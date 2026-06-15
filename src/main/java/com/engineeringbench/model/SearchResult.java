package com.engineeringbench.model;

public record SearchResult(
        String source,
        String content,
        double score
) {}