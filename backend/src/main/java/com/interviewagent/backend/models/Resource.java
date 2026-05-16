package com.interviewagent.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "resources")
public class Resource {
    @Id
    private String id;
    private String title;
    private String type; // "PDF", "TEXT", "YOUTUBE_LINK"
    private String contentUrl;
    private String userId;
    private String projectId;
    private String folderId;
    private int chunkCount; // Number of embedded RAG chunks
    private double progressPercentage; // 0.0 to 100.0
    private LocalDateTime uploadedAt;
}

