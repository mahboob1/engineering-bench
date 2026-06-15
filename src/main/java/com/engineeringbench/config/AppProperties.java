package com.engineeringbench.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String openaiKey;

    public String getOpenaiKey() {
        return openaiKey;
    }

    public void setOpenaiKey(String openaiKey) {
        this.openaiKey = openaiKey;
    }
}