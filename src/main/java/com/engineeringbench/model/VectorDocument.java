package com.engineeringbench.model;

public record VectorDocument(
        String id,
        String source,
        String content,
        float[] embedding
) {}