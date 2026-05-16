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

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewSessionRepository sessionRepository;
    private final MultiAgentOrchestratorService orchestratorService;

    @PostMapping("/start")
    public ResponseEntity<InterviewSession> startSession(@RequestParam String userId, @RequestParam String topic) {
        InterviewSession session = InterviewSession.builder()
                .userId(userId)
                .topic(topic)
                .startedAt(LocalDateTime.now())
                .transcript(new ArrayList<>())
                .build();
                
        String firstQuestion = orchestratorService.generateNextQuestion(session.getTranscript(), topic);
        session.getTranscript().add(new InterviewSession.ChatMessage("AI_INTERVIEWER", firstQuestion, LocalDateTime.now()));
        
        session = sessionRepository.save(session);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{sessionId}/message")
    public ResponseEntity<InterviewSession> sendMessage(@PathVariable String sessionId, @RequestBody String userMessage) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
                
        session.getTranscript().add(new InterviewSession.ChatMessage("USER", userMessage, LocalDateTime.now()));
        
        // Analyze response
        List<AgentFeedback> feedbacks = orchestratorService.analyzeResponse(userMessage);
        session.setAgentFeedbacks(feedbacks); // Simplified: replacing for demo
        
        // Generate next question
        String nextQuestion = orchestratorService.generateNextQuestion(session.getTranscript(), session.getTopic());
        session.getTranscript().add(new InterviewSession.ChatMessage("AI_INTERVIEWER", nextQuestion, LocalDateTime.now()));
        
        session = sessionRepository.save(session);
        return ResponseEntity.ok(session);
    }
}
