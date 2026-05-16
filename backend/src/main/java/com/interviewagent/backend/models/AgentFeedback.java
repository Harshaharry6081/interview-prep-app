package com.interviewagent.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentFeedback {
    private String agentName; // e.g., "Technical Agent", "Communication Agent"
    private double score; // out of 10
    private String feedback;
    private String suggestions;
}
