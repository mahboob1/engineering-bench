package com.engineeringbench.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QdrantConfig {

    private final QdrantProperties props;

    public QdrantConfig(QdrantProperties props) {
        this.props = props;
    }

    @Bean
    public QdrantClient qdrantClient() {

        return new QdrantClient(
                QdrantGrpcClient.newBuilder(
                        props.getQdrantHost(),
                        props.getQdrantPort(),
                        false
                ).build()
        );
    }
}