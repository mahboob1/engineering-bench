package com.engineeringbench.config;

import com.engineeringbench.agent.EngineeringAgent;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EngineeringAgentConfig {

    @Bean
    public EngineeringAgent engineeringAgent(
            AppProperties props) {

        OpenAiChatModel chatModel =
                OpenAiChatModel.builder()
                        .apiKey(props.getOpenaiKey())
                        .modelName("gpt-4o-mini")
                        .build();

        return AiServices.builder(EngineeringAgent.class)
                .chatLanguageModel(chatModel)
                .build();
    }
}