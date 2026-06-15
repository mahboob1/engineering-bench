package com.engineeringbench.service;

import com.google.common.util.concurrent.ListenableFuture;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.qdrant.client.grpc.Points.SearchPoints;
import static io.qdrant.client.grpc.Points.SearchPoints.newBuilder;

@Service
public class SemanticSearchService {

    private final EmbeddingService embeddingService;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final QdrantClient qdrantClient;

    public SemanticSearchService(
            EmbeddingService embeddingService,
            EmbeddingStore<TextSegment> embeddingStore,
            QdrantClient qdrantClient) {

        this.embeddingService = embeddingService;
        this.embeddingStore = embeddingStore;
        this.qdrantClient = qdrantClient;
    }

    public List<EmbeddingMatch<TextSegment>>
    searchEmbed(String question) {

        var queryEmbedding =
                embeddingService.embed(question);
        System.out.println(
                "Query dimensions = "
                        + queryEmbedding.vector().length
        );

        var request =
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(5)
                        .minScore(0.0)
                        .build();

        return embeddingStore
                .search(request)
                .matches();
    }

    public List<String> search(String question) {

        var queryEmbedding = embeddingService.embed(question);

        float[] embeddingArray = queryEmbedding.vector();

        List<Float> vector = new ArrayList<>(embeddingArray.length);
        for (float v : embeddingArray) {
            vector.add(v);
        }

        ListenableFuture<?> future =
                qdrantClient.searchAsync(
                        SearchPoints.newBuilder()
                                .setCollectionName("engineering_docs")
                                .addAllVector(vector)
                                .setLimit(1)
                                .setWithPayload(
                                        Points.WithPayloadSelector.newBuilder()
                                                .setEnable(true)
                                                .build()
                                )
                                .build()
                );

        Object result;
        try {
            result = future.get();
        } catch (Exception e) {
            throw new RuntimeException("Qdrant search failed", e);
        }

        System.out.println(result);

        return List.of(result.toString());
    }
}