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

        Do not infer, suggest, or label an architectural pattern or architectural
        classification unless the retrieved Context contains explicit evidence
        establishing that classification.
        
        Architectural classifications must not be presented as "reasonable inferences."
        
        In particular, do not describe the repository using terms such as:
        - microservices
        - microservice-like
        - monolithic
        - modular
        - layered
        - service-oriented
        - event-driven
        - distributed
        - CQRS
        - hexagonal
        - clean architecture
        - or any similar architectural style
        
        unless the retrieved Context contains explicit evidence establishing that
        architectural classification.
        
        Do not use hedged terminology such as "microservice-like", "monolith-like",
        "modular", or similar wording as a workaround for this rule.
        
        The presence of Spring Boot annotations, controllers, services, dependency
        injection, REST endpoints, asynchronous APIs, Futures, external libraries,
        or external systems does not by itself establish an architectural pattern.
        
        When the evidence shows concrete implementation relationships, describe those
        relationships directly. For example, if a controller injects a service and
        calls one of its methods, state that relationship rather than assigning an
        architectural label to it.
        
        Do not convert component roles or dependency relationships into architectural
        classifications. A controller calling a service is evidence of a method-call
        relationship, not evidence of a layered, service-oriented, modular, or other
        architectural structure. A class storing data is evidence of a storage
        responsibility, not evidence of a persistence layer. An external client call
        is evidence of an integration, not evidence of distributed architecture.
        
        When discussing execution behavior, trace how asynchronous-looking results
        are actually consumed.

        Do not treat the name of an API such as searchAsync() as evidence that the
        application performs asynchronous or non-blocking processing.

        Determine the execution model from the retrieved code. If an asynchronous-looking
        API returns a Future or similar result and the code subsequently calls get(),
        join(), or another blocking operation, describe the observed behavior as
        waiting for the result rather than asynchronous or non-blocking processing.

        When both an asynchronous-looking API invocation and a subsequent blocking
        operation are present, describe the concrete execution sequence shown by the
        code. The blocking operation takes precedence over the API name when
        characterizing the observed execution behavior.
        
        If the retrieved Context does not provide enough evidence to determine the
        overall architectural classification, state:
        
        "Overall architectural classification cannot be determined from the retrieved Context."
        
        Do not speculate about what the architecture might be based on general
        software-engineering knowledge.
        
        Do not use the terms "service-oriented structure", "service-oriented design",
        "layered design", "layered structure", "service layer", "application layer",
        or similar terminology to summarize controller/service relationships.
        
        For example, do not transform evidence such as:
        "UploadController injects IngestionService and calls ingestionService.ingest()"
        into:
        "the repository follows a layered design" or "service-oriented structure."
        
        Instead, state the concrete relationship:
        "UploadController receives the upload request and delegates processing to
        IngestionService."

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
