package com.engineeringbench.service;

import io.qdrant.client.QdrantClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QdrantCollectionService {

    private final QdrantClient qdrantClient;

    public QdrantCollectionService(
            QdrantClient qdrantClient) {

        this.qdrantClient = qdrantClient;
    }

    public void createCollection(String collectionName) {

    }

    public void deleteCollection(String collectionName) {

    }

    public List<String> listCollections() {
        return null;
    }
}
