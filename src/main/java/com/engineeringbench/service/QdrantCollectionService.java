package com.engineeringbench.service;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QdrantCollectionService {

    private final QdrantClient qdrantClient;

    public QdrantCollectionService(
            QdrantClient qdrantClient) {

        this.qdrantClient = qdrantClient;
    }

    public void createCollection(String collectionName) throws Exception {
        // Qdrant collections require a vector configuration upon creation.
        // This default configuration uses 1536 dimensions (standard for OpenAI text-embedding-3-small).
        Collections.VectorParams params = Collections.VectorParams.newBuilder()
                .setSize(1536)
                .setDistance(Collections.Distance.Cosine)
                .build();

        // .get() blocks until the async operation is fully completed by Qdrant
        qdrantClient.createCollectionAsync(collectionName, params).get();
    }

    public void deleteCollection(String collectionName) throws Exception {
        qdrantClient.deleteCollectionAsync(collectionName).get();
    }

    public List<String> listCollections() throws Exception {
        return qdrantClient.listCollectionsAsync().get();
    }
}
