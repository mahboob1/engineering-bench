package com.engineeringbench.service;

import com.engineeringbench.config.AppProperties;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final OpenAiChatModel chatModel;

    public ChatService(AppProperties props) {

        this.chatModel =
                OpenAiChatModel.builder()
                        .apiKey(props.getOpenaiKey())
                        .modelName("gpt-4o-mini")
                        .build();
    }

    public String answer(
            String question,
            String context) {

        String prompt = """
                Answer ONLY from the supplied context.

                Context:
                %s

                Question:
                %s
                """.formatted(
                context,
                question
        );

//        return chatModel.generate(prompt);
        return chatModel.chat(prompt);
    }
}
