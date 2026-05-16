package com.interviewagent.backend.services;

import com.interviewagent.backend.models.AgentFeedback;
import com.interviewagent.backend.models.InterviewSession;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class MultiAgentOrchestratorService {

    // Stubs for the Multi-Agent LLM interaction
    public String generateNextQuestion(List<InterviewSession.ChatMessage> transcript, String topic) {
        // In a real scenario, this would call an LLM API
        return "Can you explain the core concepts of " + topic + " and provide a real-world example?";
    }

    public List<AgentFeedback> analyzeResponse(String userResponse) {
        // Stub: In reality, we'd send the response to different LLM agents concurrently
        
        AgentFeedback techAgent = AgentFeedback.builder()
                .agentName("Technical Agent")
                .score(8.0)
                .feedback("Good grasp of basics, but lacked deep dive into advanced edge cases.")
                .suggestions("Review edge cases for " + userResponse.substring(0, Math.min(10, userResponse.length())) + "...")
                .build();
                
        AgentFeedback commAgent = AgentFeedback.builder()
                .agentName("Communication Agent")
                .score(7.5)
                .feedback("Clear articulation, but somewhat verbose.")
                .suggestions("Try the STAR method to be more concise.")
                .build();
                
        return Arrays.asList(techAgent, commAgent);
    }
}
