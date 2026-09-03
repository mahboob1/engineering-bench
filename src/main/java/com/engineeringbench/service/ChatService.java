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
            Answer ONLY from the supplied Context.

            The Context contains information retrieved from the
            selected Qdrant collection.

            If the answer is not present in the supplied Context,
            respond with exactly:

            "I could not find the answer in the selected collection."

            Do not use your general knowledge to answer the question.

            Question:
            %s

            Context:
            %s
            """.formatted(
                question,
                context
        );

        return chatModel.chat(prompt);
    }

    public String answerWithHistory(
            String question,
            String context,
            String history) {

        String prompt = """
            Answer ONLY from the supplied Context.

            The Context contains information retrieved from the
            selected Qdrant collection.

            Conversation History is provided only to help understand
            follow-up questions, references, and conversational context.

            Do NOT use Conversation History as a source of factual
            information. Repository or document facts must come only
            from the supplied Context.

            If the answer is not present in the supplied Context,
            respond with exactly:

            "I could not find the answer in the selected collection."

            Do not use your general knowledge to answer the question.

            Question:
            %s

            Context:
            %s

            Conversation History:
            %s
            """.formatted(
                question,
                context,
                history
        );

        return chatModel.chat(prompt);
    }

    public String analyze(
            String question,
            String context) {

        String prompt = """
            You are an expert software engineer and software architect.

            Analyze the supplied code and documentation to answer the
            user's engineering question.

            The Context contains information retrieved from the selected
            Qdrant collection.

            Use the supplied Context as the primary evidence.

            You may reason about:
            - relationships between classes and components
            - dependencies
            - control flow
            - responsibilities
            - APIs
            - configuration
            - data flow
            - architectural implications
            - potential risks
            - likely changes required to implement a feature

            Clearly distinguish between:
            - Facts directly supported by the Context
            - Reasonable inferences from the Context
            - Information that cannot be determined from the Context

            Do not invent files, classes, APIs, dependencies,
            configurations, or behavior that are not supported by
            the Context.

            Do not use general knowledge as evidence about this
            specific repository.

            Provide a structured engineering analysis.

            Question / Feature Request:
            %s

            Context:
            %s
            """.formatted(
                question,
                context
        );

        return chatModel.chat(prompt);
    }
}
