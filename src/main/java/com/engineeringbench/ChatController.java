package com.engineeringbench;

import com.engineeringbench.service.RetrievalService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

  private final RetrievalService retrievalService;

  public ChatController(RetrievalService retrievalService) {
    this.retrievalService = retrievalService;
  }

  @PostMapping("/chat")
  public String chat(@RequestBody QuestionRequest req) {
    return retrievalService.answer(req.question());
  }

  record QuestionRequest(String question) {}
}