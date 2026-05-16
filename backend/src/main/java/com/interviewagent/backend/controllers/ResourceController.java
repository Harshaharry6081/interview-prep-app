package com.interviewagent.backend.controllers;

import com.interviewagent.backend.models.Resource;
import com.interviewagent.backend.repositories.ResourceRepository;
import com.interviewagent.backend.services.DocumentParsingService;
import com.interviewagent.backend.services.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceRepository resourceRepository;
    private final DocumentParsingService parsingService;
    private final EmbeddingService embeddingService;

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<Resource>> getResourcesByFolder(@PathVariable String folderId) {
        return ResponseEntity.ok(resourceRepository.findByFolderId(folderId));
    }

    /**
     * Upload one or multiple files, attach to a folder,
     * parse and embed them for RAG.
     */
    @PostMapping("/upload")
    public ResponseEntity<List<Resource>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("folderId") String folderId,
            @RequestParam(value = "projectId", required = false) String projectId,
            Authentication auth) {

        List<Resource> savedResources = new ArrayList<>();

        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
            String contentType = file.getContentType() != null ? file.getContentType() : "text/plain";
            String type = filename.toLowerCase().endsWith(".pdf") ? "PDF" : "TEXT";

            try {
                // Parse and embed for RAG
                List<String> chunks = parsingService.extractAndChunkText(file);
                embeddingService.storeChunks(chunks, folderId, filename);
                log.info("Processed {} chunks from file: {}", chunks.size(), filename);

                Resource resource = Resource.builder()
                        .title(filename)
                        .type(type)
                        .folderId(folderId)
                        .projectId(projectId)
                        .userId(auth.getName())
                        .chunkCount(chunks.size())
                        .progressPercentage(0.0)
                        .uploadedAt(LocalDateTime.now())
                        .build();

                savedResources.add(resourceRepository.save(resource));
            } catch (Exception e) {
                log.error("Failed to process file {}: {}", filename, e.getMessage());
            }
        }

        return ResponseEntity.ok(savedResources);
    }

    @PostMapping("/link")
    public ResponseEntity<Resource> addYouTubeLink(
            @RequestBody java.util.Map<String, String> body,
            Authentication auth) {

        Resource resource = Resource.builder()
                .title(body.getOrDefault("title", "YouTube Resource"))
                .type("YOUTUBE_LINK")
                .contentUrl(body.get("url"))
                .folderId(body.get("folderId"))
                .userId(auth.getName())
                .progressPercentage(0.0)
                .uploadedAt(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(resourceRepository.save(resource));
    }
}
