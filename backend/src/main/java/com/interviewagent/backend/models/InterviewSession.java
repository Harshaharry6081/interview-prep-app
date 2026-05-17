package com.interviewagent.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "interview_sessions")
public class InterviewSession {
    @Id
    private String id;
    private String userId;
    private String topic;
    private String interviewGroup; // SYSTEM_DESIGN, BACKEND, FRONTEND, HR, DSA
    private String difficulty;     // EASY, MEDIUM, HARD
    private List<ChatMessage> transcript;
    private List<AgentFeedback> agentFeedbacks;
    private String finalAssessment;
    private Double overallScore;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role; // "USER", "AI_INTERVIEWER"
        private String content;
        private LocalDateTime timestamp;
    }
}
