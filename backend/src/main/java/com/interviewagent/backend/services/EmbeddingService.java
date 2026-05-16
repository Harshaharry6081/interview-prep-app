package com.interviewagent.backend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

/**
 * EmbeddingService: Handles storing document chunks into ChromaDB
 * and performing similarity searches for the RAG system.
 *
 * Embeddings are generated using a simple TF-IDF-like hash approach
 * for the local/no-LLM-API case. Replace generateEmbedding() with
 * a real API call (e.g., OpenAI /embeddings) for production.
 */
@Slf4j
@Service
public class EmbeddingService {

    private final WebClient chromaClient;
    private static final int EMBEDDING_DIM = 384;

    public EmbeddingService(@Value("${chroma.host:http://localhost:8000}") String chromaHost) {
        this.chromaClient = WebClient.builder().baseUrl(chromaHost).build();
        ensureCollectionExists();
    }

    private void ensureCollectionExists() {
        try {
            chromaClient.post()
                    .uri("/api/v1/collections")
                    .bodyValue(Map.of("name", "interview_docs", "metadata", Map.of()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.info("Collection may already exist or ChromaDB not yet available: {}", e.getMessage());
        }
    }

    public void storeChunks(List<String> chunks, String folderId, String documentTitle) {
        if (chunks.isEmpty()) return;

        List<List<Double>> embeddings = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        List<Map<String, String>> metadatas = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            embeddings.add(generateEmbedding(chunks.get(i)));
            ids.add(folderId + "_" + documentTitle + "_chunk_" + i);
            metadatas.add(Map.of("folderId", folderId, "source", documentTitle, "chunkIndex", String.valueOf(i)));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("ids", ids);
        body.put("embeddings", embeddings);
        body.put("documents", chunks);
        body.put("metadatas", metadatas);

        try {
            chromaClient.post()
                    .uri("/api/v1/collections/interview_docs/add")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Stored {} chunks for document: {} in folder: {}", chunks.size(), documentTitle, folderId);
        } catch (Exception e) {
            log.error("Failed to store chunks in ChromaDB: {}", e.getMessage());
        }
    }

    public List<String> queryRelevantContext(String question, String folderId, int topK) {
        List<Double> queryEmbedding = generateEmbedding(question);

        Map<String, Object> body = new HashMap<>();
        body.put("query_embeddings", List.of(queryEmbedding));
        body.put("n_results", topK);
        body.put("where", Map.of("folderId", folderId));

        try {
            Map<?, ?> result = chromaClient.post()
                    .uri("/api/v1/collections/interview_docs/query")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (result != null && result.containsKey("documents")) {
                List<?> docs = (List<?>) result.get("documents");
                if (!docs.isEmpty() && docs.get(0) instanceof List) {
                    List<?> inner = (List<?>) docs.get(0);
                    List<String> contexts = new ArrayList<>();
                    for (Object doc : inner) {
                        contexts.add(doc.toString());
                    }
                    return contexts;
                }
            }
        } catch (Exception e) {
            log.error("ChromaDB query failed: {}", e.getMessage());
        }
        return List.of();
    }

    /**
     * Simple deterministic embedding for local/demo use.
     * Replace with a real embedding model API for production.
     */
    private List<Double> generateEmbedding(String text) {
        List<Double> embedding = new ArrayList<>(Collections.nCopies(EMBEDDING_DIM, 0.0));
        String[] words = text.toLowerCase().replaceAll("[^a-z0-9 ]", "").split("\\s+");
        for (String word : words) {
            int hash = Math.abs(word.hashCode()) % EMBEDDING_DIM;
            embedding.set(hash, embedding.get(hash) + 1.0 / words.length);
        }
        return embedding;
    }
}
