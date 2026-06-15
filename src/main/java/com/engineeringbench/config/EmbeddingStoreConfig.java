package com.engineeringbench.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingStoreConfig {

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(
            QdrantClient qdrantClient) {

        return QdrantEmbeddingStore.builder()
                .client(qdrantClient)
                .collectionName("engineering_docs")
                .build();
    }
}