package com.interviewagent.backend.services;

import com.interviewagent.backend.models.AgentFeedback;
import com.interviewagent.backend.models.InterviewSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MultiAgentOrchestratorService {

    private final WebClient geminiClient;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    public MultiAgentOrchestratorService() {
        this.geminiClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    /**
     * Generates the next interview question using Gemini, based on the transcript history and topic/group.
     */
    public String generateNextQuestion(List<InterviewSession.ChatMessage> transcript, String topic) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return "Can you explain the core concepts of " + topic + " and provide a real-world example?";
        }

        try {
            String history = transcript.stream()
                    .map(m -> m.getRole() + ": " + m.getContent())
                    .collect(Collectors.joining("\n"));

            String systemPrompt = """
                    You are a senior technical interviewer conducting a professional job interview.
                    Your role is '%s' interview. Be conversational, professional, and progressive — start with fundamentals
                    then move to advanced topics as the conversation progresses.
                    Ask ONE focused question at a time. Do NOT include greetings or filler text. Just ask the question directly.
                    Based on the conversation so far, ask the next most appropriate question.
                    """.formatted(topic);

            String userPrompt = history.isBlank()
                    ? "Start the interview with a good opening question for a " + topic + " interview."
                    : "Conversation so far:\n" + history + "\n\nAsk the next interview question.";

            return callGemini(systemPrompt, userPrompt);
        } catch (Exception e) {
            log.error("Gemini question generation failed: {}", e.getMessage());
            return "Can you walk me through your experience with " + topic + "?";
        }
    }

    /**
     * Analyzes the candidate's response using multiple AI agent personas via Gemini.
     */
    public List<AgentFeedback> analyzeResponse(String userResponse, String topic) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return buildStubFeedback();
        }

        try {
            AgentFeedback techAgent   = runAgent("Technical Accuracy Agent",
                    "You are a strict technical evaluator. Rate the technical accuracy and depth of this answer for a " + topic + " interview. " +
                    "Score from 0-10. Be specific about what was correct and what was missing. " +
                    "Respond in JSON: {\"score\": X.X, \"feedback\": \"...\", \"suggestions\": \"...\"}",
                    userResponse);

            AgentFeedback commAgent   = runAgent("Communication Coach",
                    "You are a communication expert. Evaluate the clarity, structure, and conciseness of this answer. " +
                    "Score from 0-10. Suggest communication improvements. " +
                    "Respond in JSON: {\"score\": X.X, \"feedback\": \"...\", \"suggestions\": \"...\"}",
                    userResponse);

            AgentFeedback domainAgent = runAgent("Domain Depth Agent",
                    "You are a domain expert for " + topic + ". Evaluate the depth of domain knowledge shown. " +
                    "Score from 0-10. Identify gaps or misconceptions. " +
                    "Respond in JSON: {\"score\": X.X, \"feedback\": \"...\", \"suggestions\": \"...\"}",
                    userResponse);

            return Arrays.asList(techAgent, commAgent, domainAgent);
        } catch (Exception e) {
            log.error("Gemini analysis failed: {}", e.getMessage());
            return buildStubFeedback();
        }
    }

    private AgentFeedback runAgent(String agentName, String systemPrompt, String userContent) {
        try {
            String response = callGemini(systemPrompt,
                    "Analyze this answer and respond ONLY with valid JSON:\n\n" + userContent);

            // Extract JSON from response (Gemini sometimes wraps in markdown)
            String cleaned = response.replaceAll("```json", "").replaceAll("```", "").trim();

            // Simple manual parse to avoid extra dependencies
            double score = extractDouble(cleaned, "score");
            String feedback    = extractString(cleaned, "feedback");
            String suggestions = extractString(cleaned, "suggestions");

            return AgentFeedback.builder()
                    .agentName(agentName)
                    .score(score)
                    .feedback(feedback)
                    .suggestions(suggestions)
                    .build();
        } catch (Exception e) {
            log.warn("Agent {} failed: {}", agentName, e.getMessage());
            return AgentFeedback.builder()
                    .agentName(agentName)
                    .score(7.0)
                    .feedback("Analysis unavailable at this time.")
                    .suggestions("Please retry.")
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private String callGemini(String systemPrompt, String userPrompt) {
        String url = "/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

        Map<String, Object> systemInstruction = Map.of(
                "parts", List.of(Map.of("text", systemPrompt))
        );
        Map<String, Object> content = Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userPrompt))
        );
        Map<String, Object> requestBody = Map.of(
                "system_instruction", systemInstruction,
                "contents", List.of(content)
        );

        Map<String, Object> response = (Map<String, Object>) geminiClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) throw new RuntimeException("Null response from Gemini");

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        Map<String, Object> firstCandidate = candidates.get(0);
        Map<String, Object> content2 = (Map<String, Object>) firstCandidate.get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content2.get("parts");
        return (String) parts.get(0).get("text");
    }

    private double extractDouble(String json, String key) {
        try {
            int idx = json.indexOf("\"" + key + "\"");
            if (idx == -1) return 7.0;
            String sub = json.substring(idx + key.length() + 3).trim();
            int end = sub.indexOf(',');
            if (end == -1) end = sub.indexOf('}');
            return Double.parseDouble(sub.substring(0, end).trim());
        } catch (Exception e) { return 7.0; }
    }

    private String extractString(String json, String key) {
        try {
            int idx = json.indexOf("\"" + key + "\"");
            if (idx == -1) return "";
            String sub = json.substring(idx + key.length() + 3);
            int start = sub.indexOf('"') + 1;
            int end   = sub.indexOf('"', start);
            return sub.substring(start, end);
        } catch (Exception e) { return ""; }
    }

    private List<AgentFeedback> buildStubFeedback() {
        return Arrays.asList(
                AgentFeedback.builder().agentName("Technical Accuracy Agent").score(8.0)
                        .feedback("Good grasp of basics, but lacked advanced edge cases.")
                        .suggestions("Review edge cases and internals of the concept.").build(),
                AgentFeedback.builder().agentName("Communication Coach").score(7.5)
                        .feedback("Clear articulation, but somewhat verbose.")
                        .suggestions("Try the STAR method to be more concise.").build(),
                AgentFeedback.builder().agentName("Domain Depth Agent").score(7.8)
                        .feedback("Solid domain awareness but could go deeper on internals.")
                        .suggestions("Study implementation details alongside the theory.").build()
        );
    }
}
