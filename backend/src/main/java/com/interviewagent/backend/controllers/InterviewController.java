package com.interviewagent.backend.controllers;

import com.interviewagent.backend.models.AgentFeedback;
import com.interviewagent.backend.models.InterviewSession;
import com.interviewagent.backend.repositories.InterviewSessionRepository;
import com.interviewagent.backend.services.MultiAgentOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewSessionRepository sessionRepository;
    private final MultiAgentOrchestratorService orchestratorService;

    @PostMapping("/start")
    public ResponseEntity<InterviewSession> startSession(
            @RequestParam String userId,
            @RequestParam String topic,
            @RequestParam(defaultValue = "GENERAL") String interviewGroup,
            @RequestParam(defaultValue = "MEDIUM") String difficulty) {

        InterviewSession session = InterviewSession.builder()
                .userId(userId)
                .topic(topic)
                .interviewGroup(interviewGroup)
                .difficulty(difficulty)
                .startedAt(LocalDateTime.now())
                .transcript(new ArrayList<>())
                .agentFeedbacks(new ArrayList<>())
                .build();

        String firstQuestion = orchestratorService.generateNextQuestion(session.getTranscript(), topic);
        session.getTranscript().add(new InterviewSession.ChatMessage("AI_INTERVIEWER", firstQuestion, LocalDateTime.now()));

        session = sessionRepository.save(session);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{sessionId}/message")
    public ResponseEntity<InterviewSession> sendMessage(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body) {

        String userMessage = body.get("message");
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        // Add user message
        session.getTranscript().add(new InterviewSession.ChatMessage("USER", userMessage, LocalDateTime.now()));

        // Analyze response with multi-agent AI
        List<AgentFeedback> feedbacks = orchestratorService.analyzeResponse(userMessage, session.getTopic());
        session.setAgentFeedbacks(feedbacks);

        // Calculate overall score from agent feedback
        OptionalDouble avg = feedbacks.stream().mapToDouble(AgentFeedback::getScore).average();
        session.setOverallScore(avg.isPresent() ? Math.round(avg.getAsDouble() * 10.0) / 10.0 : null);

        // Generate follow-up question
        String nextQuestion = orchestratorService.generateNextQuestion(session.getTranscript(), session.getTopic());
        session.getTranscript().add(new InterviewSession.ChatMessage("AI_INTERVIEWER", nextQuestion, LocalDateTime.now()));

        session = sessionRepository.save(session);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<InterviewSession> endSession(@PathVariable String sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        session.setCompletedAt(LocalDateTime.now());

        // Generate final assessment using Gemini
        String transcript = session.getTranscript().stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .reduce("", (a, b) -> a + "\n" + b);

        String assessment = orchestratorService.generateNextQuestion(
                List.of(new InterviewSession.ChatMessage("USER",
                        "Provide a final performance summary for this interview. Transcript:\n" + transcript, LocalDateTime.now())),
                "final assessment");
        session.setFinalAssessment(assessment);

        session = sessionRepository.save(session);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/history")
    public ResponseEntity<List<InterviewSession>> getHistory(@RequestParam String userId) {
        List<InterviewSession> history = sessionRepository.findByUserIdOrderByStartedAtDesc(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<InterviewSession> getSession(@PathVariable String sessionId) {
        return sessionRepository.findById(sessionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
