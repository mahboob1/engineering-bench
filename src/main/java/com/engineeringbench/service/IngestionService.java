package com.engineeringbench.service;

import com.engineeringbench.model.DocumentChunk;
import com.engineeringbench.store.InMemoryChunkStore;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class IngestionService {

    private final TextChunker chunker;
    private final InMemoryChunkStore store;

    private final EmbeddingService embeddingService;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public IngestionService(
            TextChunker chunker,
            InMemoryChunkStore store,
            EmbeddingService embeddingService,
            EmbeddingStore<TextSegment> embeddingStore) {

        this.chunker = chunker;
        this.store = store;
        this.embeddingService = embeddingService;
        this.embeddingStore = embeddingStore;
    }

    public void ingest(
            MultipartFile file)
            throws Exception {

        String text =
                extractText(file);

        List<String> chunkTexts =
                chunker.chunk(
                        text,
                        800,
                        100);

        List<DocumentChunk> chunks =
                new ArrayList<>();

        for (String c : chunkTexts) {

            chunks.add(
                    new DocumentChunk(
                            UUID.randomUUID().toString(),
                            file.getOriginalFilename(),
                            c
                    )
            );

            var embedding =
                    embeddingService.embed(c);

            Metadata metadata = new Metadata();

            metadata.put(
                    "source",
                    file.getOriginalFilename()
            );

            var segment =
                    TextSegment.from(c, metadata);

            embeddingStore.add(
                    embedding,
                    segment
            );

            System.out.println(
                    "Stored chunk in Qdrant. Length="
                            + c.length()
            );

        }

        store.saveAll(chunks);
    }

    private String extractText(
            MultipartFile file)
            throws Exception {

        if (file.getOriginalFilename()
                .endsWith(".pdf")) {

            var pdf =
                    Loader.loadPDF(
                            file.getBytes());

            return new PDFTextStripper()
                    .getText(pdf);
        }

        return new String(
                file.getBytes());
    }
}