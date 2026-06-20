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

        ingestText(file.getOriginalFilename(),
                file.getOriginalFilename(),
                text);

// in memory store, not needed to save as we are directly adding to embedding store
       /*
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
        } */

//        store.saveAll(chunks); in memory store, not needed to save as we are directly adding to embedding store
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

    public void ingestText(
            String repository,
            String source,
            String text) {

        List<String> chunkTexts =
                chunker.chunk(
                        text,
                        800,
                        100);

        for (String c : chunkTexts) {
            Metadata metadata =
                    new Metadata();
            metadata.put(
                    "repository",
                    repository);

            metadata.put(
                    "source",
                    source);

            TextSegment segment =
                    TextSegment.from(
                            c,
                            metadata);

            var embedding =
                    embeddingService.embed(c);

            embeddingStore.add(
                    embedding,
                    segment);
            System.out.println(
                    "Stored chunk in Qdrant. Length="
                            + c.length()
            );
        }
    }
}