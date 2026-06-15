package com.engineeringbench.store;

import com.engineeringbench.model.DocumentChunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InMemoryChunkStore {

    private final List<DocumentChunk> chunks =
            new ArrayList<>();

    public void saveAll(List<DocumentChunk> newChunks) {
        for (DocumentChunk c : newChunks) {

            boolean exists = chunks.stream()
                    .anyMatch(e ->
                            e.content().equals(c.content()));

            if (!exists) {
                chunks.add(c);
            }
        }
    }

    public List<DocumentChunk> getAll() {
        return chunks;
    }
}