package com.engineeringbench.service;

import com.engineeringbench.config.AppProperties;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

    private final OpenAiEmbeddingModel model;

    public EmbeddingService(AppProperties props) {

        this.model =
                OpenAiEmbeddingModel.builder()
                        .apiKey(props.getOpenaiKey())
                        .modelName("text-embedding-3-small")
                        .build();
    }

    public Embedding embed(String text) {

        return model.embed(text)
                .content();
    }
}