package com.engineeringbench;

import com.engineeringbench.model.ChatMessage;
import com.engineeringbench.model.ChatRequest;
import com.engineeringbench.model.SearchResult;
import com.engineeringbench.service.ChatMemoryService;
import com.engineeringbench.service.ChatService;
import com.engineeringbench.service.SemanticSearchService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AskController {

    private final SemanticSearchService searchService;
    private final ChatService chatService;
    private final ChatMemoryService
            memoryService;

    public AskController(
            SemanticSearchService searchService,
            ChatService chatService,
            ChatMemoryService memoryService) {

        this.searchService = searchService;
        this.chatService = chatService;
        this.memoryService = memoryService;
    }

    @GetMapping("/askString")
    public String askString(
            @RequestParam String question, @RequestParam(required = false) String repository) {

        List<String> chunks =
                searchService.searchString(question);

        String context =
                String.join("\n", chunks);

        return chatService.answer(
                question,
                context,
                repository
        );
    }

    @GetMapping("/ask")
    public String ask(
            @RequestParam String question, @RequestParam(required = false) String repository) {

        List<SearchResult> chunks =
                searchService.search(question, repository);

        String context =
                chunks.stream()
                        .map(c ->
                                "Source: " + c.source()
                                        + "\n"
                                        + c.content())
                        .collect(Collectors.joining("\n\n"));

        String answer = chatService.answer(
                question,
                context,
                repository
        );

        String citations =
                chunks.stream()
                        .map(SearchResult::source)
                        .distinct()
                        .collect(Collectors.joining("\n"));

        return answer
                + "\n\nSources:\n"
                + citations;
    }

    @PostMapping("/chat")
    public String chat(
            @RequestBody
            ChatRequest request) {

        var session =
                memoryService.getSession(
                        request.sessionId());

        List<SearchResult> chunks =
                searchService.search(
                        request.question(),
                        request.repository());

        String history =
                session.getMessages()
                        .stream()
                        .map(m ->
                                m.role()
                                        + ": "
                                        + m.content())
                        .collect(
                                Collectors.joining(
                                        "\n"));

        String context =
                chunks.stream()
                        .map(c ->
                                "Repository: "
                                        + c.repository()
                                        + "\nSource: "
                                        + c.source()
                                        + "\n"
                                        + c.content())
                        .collect(
                                Collectors.joining(
                                        "\n\n"));

        String answer =
                chatService.answer(
                        request.question(),
                        context,
                        history);

        session.add(
                new ChatMessage(
                        "user",
                        request.question()
                )
        );

        session.add(
                new ChatMessage(
                        "assistant",
                        answer
                )
        );

        return answer;
    }
}