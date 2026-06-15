package com.engineeringbench;

import com.engineeringbench.service.EmbeddingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    private final EmbeddingService embeddingService;

    public TestController(
            EmbeddingService embeddingService) {

        this.embeddingService = embeddingService;
    }

    @GetMapping("/embed")
    public String embed(
            @RequestParam String text) {

        var embedding =
                embeddingService.embed(text);

        return "Dimensions: "
                + embedding.vector().length;
    }

    @GetMapping("/embed-size")
    public String embedSize(
            @RequestParam String text) {

        var embedding =
                embeddingService.embed(text);

        return String.valueOf(
                embedding.vector().length
        );
    }
}