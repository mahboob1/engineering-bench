package com.engineeringbench.model;

import java.util.ArrayList;
import java.util.List;

public class ChatSession {

    private final List<ChatMessage>
            messages =
            new ArrayList<>();

    public void add(
            ChatMessage message) {

        messages.add(message);
    }

    public List<ChatMessage>
    getMessages() {

        return messages;
    }
}