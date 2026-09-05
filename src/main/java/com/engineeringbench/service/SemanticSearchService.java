package com.engineeringbench.service;

import com.engineeringbench.model.SearchResult;
import com.engineeringbench.model.AnalysisCategory;
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
    private final QdrantClient qdrantClient;

    public SemanticSearchService(
            EmbeddingService embeddingService,
            QdrantClient qdrantClient) {

        this.embeddingService = embeddingService;
        this.qdrantClient = qdrantClient;
    }

//    public List<EmbeddingMatch<TextSegment>>
//    searchEmbed(String question) {
//
//        var queryEmbedding =
//                embeddingService.embed(question);
//        System.out.println(
//                "Query dimensions = "
//                        + queryEmbedding.vector().length
//        );
//
//        var request =
//                EmbeddingSearchRequest.builder()
//                        .queryEmbedding(queryEmbedding)
//                        .maxResults(5)
//                        .minScore(0.0)
//                        .build();
//
//        return embeddingStore
//                .search(request)
//                .matches();
//    }

    public List<String> searchString(String question) {

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

    public List<SearchResult> search(String collection, String question, String repository) {

        var queryEmbedding = embeddingService.embed(question);

        float[] embeddingArray = queryEmbedding.vector();

        List<Float> vector = new ArrayList<>(embeddingArray.length);
        for (float v : embeddingArray) {
            vector.add(v);
        }

        //ListenableFuture<?> future =
                //qdrantClient.searchAsync(
        SearchPoints.Builder builder = SearchPoints.newBuilder()
                                .setCollectionName(collection)
                                .addAllVector(vector)
                                .setLimit(5)
                                .setWithPayload(
                                        Points.WithPayloadSelector.newBuilder()
                                                .setEnable(true)
                                                .build()
                                );;

        if (repository != null &&
                !repository.isBlank()) {

            builder.setFilter(
                    Points.Filter.newBuilder()
                            .addMust(
                                    Points.Condition.newBuilder()
                                            .setField(
                                                    Points.FieldCondition
                                                            .newBuilder()
                                                            .setKey(
                                                                    "repository")
                                                            .setMatch(
                                                                    Points.Match
                                                                            .newBuilder()
                                                                            .setKeyword(
                                                                                    repository)
                                                                            .build()
                                                            )
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );
        }

        ListenableFuture<?> future =
                qdrantClient.searchAsync(
                        builder.build()
                );
//        Object result;
//        Points.SearchResponse response;
        List<Points.ScoredPoint> points;
        try {
//            result = future.get();
//            response =
//                    (Points.SearchResponse) future.get();
            points =
                    (List<Points.ScoredPoint>) future.get();
        } catch (Exception e) {
            throw new RuntimeException("Qdrant search failed", e);
        }

//        System.out.println(result);

//        return List.of(result.toString());
        List<SearchResult> results =
                new ArrayList<>();

        for (var point : points) {

            String content =
                    point.getPayloadMap()
                            .getOrDefault("text_segment", null)
                            .getStringValue();

            String source =
                    point.getPayloadMap()
                            .getOrDefault("source", null)
                            .getStringValue();

            String repositoryName =
                    point.getPayloadMap()
                            .getOrDefault("repository", null)==null?"":point.getPayloadMap().getOrDefault("repository", null)
                            .getStringValue();

            results.add(
                    new SearchResult(
                            content,
                            source,
                            repositoryName,
                            point.getScore()
                    )
            );
        }

        return results;
    }

    public List<SearchResult> searchForAnalysis(
            String collection,
            String question,
            String repository) {

        List<AnalysisCategory> categories = List.of(
                new AnalysisCategory(
                        "question",
                        question
                ),
                new AnalysisCategory(
                        "controllers",
                        question + " controllers REST endpoints APIs request handling"
                ),
                new AnalysisCategory(
                        "services",
                        question + " services business logic application flow"
                ),
                new AnalysisCategory(
                        "configuration",
                        question + " configuration dependencies Spring Boot application setup"
                ),
                new AnalysisCategory(
                        "persistence",
                        question + " data storage database Qdrant persistence embedding"
                ),
                new AnalysisCategory(
                        "ingestion",
                        question + " ingestion processing files repositories data flow"
                )
        );

        List<SearchResult> results =
                new ArrayList<>();

        int resultsPerCategory = 5;

        for (AnalysisCategory category : categories) {

            List<SearchResult> categoryResults =
                    search(
                            collection,
                            category.query(),
                            repository
                    );

            List<SearchResult> uniqueCategoryResults =
                    categoryResults.stream()
                            .filter(result ->
                                    results.stream()
                                            .noneMatch(existing ->
                                                    existing.source()
                                                            .equals(result.source())
                                                            &&
                                                            existing.content()
                                                                    .equals(result.content())
                                            )
                            )
                            .limit(resultsPerCategory)
                            .toList();

            results.addAll(uniqueCategoryResults);
        }

        return results;
    }
}