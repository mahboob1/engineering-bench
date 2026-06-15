package com.engineeringbench;

import com.engineeringbench.service.ChatService;
import com.engineeringbench.service.SemanticSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/ask")
    public String ask(
            @RequestParam String question) {

        List<String> chunks =
                searchService.search(question);

        String context =
                String.join("\n", chunks);

        return chatService.answer(
                question,
                context
        );
    }
}