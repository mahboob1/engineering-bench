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

        Analyze the supplied software repository evidence to answer
        the user's engineering question.

        The Context contains files/chunks retrieved from the selected
        Qdrant collection using multiple analysis-oriented searches.

        The Context may NOT represent the entire repository.

        Do not describe the repository as a whole unless the supplied
        Context provides sufficient evidence.

        Use the supplied Context as the primary evidence.

        Clearly distinguish between:

        1. Facts directly supported by the retrieved Context
        2. Reasonable inferences from the retrieved Context
        3. Unknowns that cannot be determined from the retrieved Context

        If there is insufficient evidence for an important conclusion,
        explicitly state:

        "Insufficient repository context to determine this."

        Do not invent files, classes, APIs, dependencies,
        configurations, database behavior, or application behavior.

        Do not infer or label architectural patterns such as microservices,
        event-driven architecture, CQRS, hexagonal architecture, clean
        architecture, layered architecture, or distributed systems unless
        the retrieved repository evidence provides specific, concrete
        support for that pattern.
        
        Do not use general software-engineering knowledge as evidence that
        a pattern exists in this repository.

        When discussing architecture, identify relationships between
        components only when those relationships are supported by
        the retrieved code.

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
