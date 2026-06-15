package com.engineeringbench;

import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final EmbeddingStore embeddingStore;

    public HealthController(
            EmbeddingStore embeddingStore) {

        this.embeddingStore = embeddingStore;
    }

    @GetMapping("/api/store")
    public String store() {
        return embeddingStore.getClass().getName();
    }
}