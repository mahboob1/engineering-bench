package com.engineeringbench;

import com.engineeringbench.service.SemanticSearchService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SearchController {

    private final SemanticSearchService service;

    public SearchController(
            SemanticSearchService service) {

        this.service = service;
    }

//    @GetMapping("/search")
//    public List<String> search(
//            @RequestParam String question) {
//
//        return service.search(question)
//                .stream()
//                .map(match ->
//                        match.embedded().text())
//                .toList();
//    }

    @GetMapping("/search")
    public List<String> search(@RequestParam String question) {
        return service.search(question);
    }
}