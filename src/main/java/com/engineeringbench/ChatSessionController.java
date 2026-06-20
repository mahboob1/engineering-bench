package com.engineeringbench;

import com.engineeringbench.service.ChatMemoryService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ChatSessionController {

    private final ChatMemoryService memoryService;

    public ChatSessionController(
            ChatMemoryService memoryService) {

        this.memoryService = memoryService;
    }

    @PostMapping("/session")
    public Map<String, String> createSession() {

        String sessionId =
                UUID.randomUUID()
                        .toString();

        memoryService.createSession(
                sessionId);

        return Map.of(
                "sessionId",
                sessionId
        );
    }
}