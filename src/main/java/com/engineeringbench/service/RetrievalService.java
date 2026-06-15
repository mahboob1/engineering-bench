package com.engineeringbench.service;

import com.engineeringbench.store.InMemoryChunkStore;
import org.springframework.stereotype.Service;

@Service
public class RetrievalService {

    private final InMemoryChunkStore store;

    public RetrievalService(
            InMemoryChunkStore store) {

        this.store = store;
    }

    public String answer(
            String question) {

        return store.getAll()
                .stream()
                .filter(c ->
                        c.content()
                                .toLowerCase()
                                .contains(
                                        question.toLowerCase()))
                .limit(5)
                .map(c ->
                        "[" + c.source()
                                + "]\n"
                                + c.content())
                .reduce(
                        "",
                        (a,b) -> a+"\n\n"+b
                );
    }
}