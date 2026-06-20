package com.engineeringbench.service;

import com.engineeringbench.model.ChatSession;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatMemoryService {

    private final Map<String,
            ChatSession>
            sessions =
            new ConcurrentHashMap<>();

    public ChatSession createSession(
            String sessionId) {

        ChatSession session =
                new ChatSession();

        sessions.put(
                sessionId,
                session
        );

        return session;
    }

    public ChatSession getSession(
            String sessionId) {

        return sessions.computeIfAbsent(
                sessionId,
                id -> new ChatSession()
        );
    }
}