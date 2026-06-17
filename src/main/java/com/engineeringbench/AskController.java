package com.engineeringbench;

import com.engineeringbench.model.SearchResult;
import com.engineeringbench.service.ChatService;
import com.engineeringbench.service.SemanticSearchService;
import org.springframework.web.bind.annotation.GetMapping;
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

    public AskController(
            SemanticSearchService searchService,
            ChatService chatService) {

        this.searchService = searchService;
        this.chatService = chatService;
    }

    @GetMapping("/askString")
    public String askString(
            @RequestParam String question) {

        List<String> chunks =
                searchService.searchString(question);

        String context =
                String.join("\n", chunks);

        return chatService.answer(
                question,
                context
        );
    }

    @GetMapping("/ask")
    public String ask(
            @RequestParam String question) {

        List<SearchResult> chunks =
                searchService.search(question);

        String context =
                chunks.stream()
                        .map(c ->
                                "Source: " + c.source()
                                        + "\n"
                                        + c.content())
                        .collect(Collectors.joining("\n\n"));

        String answer = chatService.answer(
                question,
                context
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
}