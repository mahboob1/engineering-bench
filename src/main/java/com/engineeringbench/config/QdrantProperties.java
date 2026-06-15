package com.engineeringbench.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class QdrantProperties {

    private String qdrantHost;
    private Integer qdrantPort;

    public String getQdrantHost() {
        return qdrantHost;
    }

    public void setQdrantHost(String qdrantHost) {
        this.qdrantHost = qdrantHost;
    }

    public Integer getQdrantPort() {
        return qdrantPort;
    }

    public void setQdrantPort(Integer qdrantPort) {
        this.qdrantPort = qdrantPort;
    }
}