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
            String history,
            String question,
            String context) {

        String prompt = """
                Conversation:
                %s
                
                Answer ONLY from the supplied context.

                If the answer is not present in the context,
                respond with:
                
                "I could not find the answer in the repository."
                  
                Question:
                %s

                Context:
                %s
                
                """.formatted(
                history,
                question,
                context
        );

        return chatModel.chat(prompt);
    }
}
