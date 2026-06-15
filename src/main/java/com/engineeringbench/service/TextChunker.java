package com.engineeringbench.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunker {

    public List<String> chunk(
            String text,
            int size,
            int overlap) {

        List<String> result = new ArrayList<>();

        int start = 0;

        while (start < text.length()) {

            int end =
                    Math.min(start + size,
                            text.length());

            result.add(
                    text.substring(start, end)
            );

            start += (size - overlap);
        }

        return result;
    }
}