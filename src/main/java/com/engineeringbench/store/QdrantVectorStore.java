package com.engineeringbench.store;

import dev.langchain4j.data.embedding.Embedding;
import io.qdrant.client.QdrantClient;
import org.springframework.stereotype.Service;

@Service
public class QdrantVectorStore {

    private final QdrantClient client;

    public QdrantVectorStore(
            QdrantClient client) {

        this.client = client;
    }
}