package com.interviewagent.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class InterviewGroupController {

    public static final List<Map<String, Object>> GROUPS = List.of(
        Map.of(
            "id", "SYSTEM_DESIGN",
            "title", "System Design",
            "icon", "🏗️",
            "description", "Design scalable distributed systems, microservices, databases, and cloud architecture.",
            "topics", List.of("Microservices", "Load Balancing", "Caching", "Databases", "Message Queues", "API Design"),
            "color", "#6366f1"
        ),
        Map.of(
            "id", "BACKEND",
            "title", "Backend Engineering",
            "icon", "⚙️",
            "description", "Spring Boot, REST APIs, concurrency, JVM internals, and backend fundamentals.",
            "topics", List.of("Spring Boot", "REST API", "Concurrency", "JVM", "SQL", "Design Patterns"),
            "color", "#8b5cf6"
        ),
        Map.of(
            "id", "FRONTEND",
            "title", "Frontend Engineering",
            "icon", "🖥️",
            "description", "Angular, React, TypeScript, browser internals, and frontend architecture.",
            "topics", List.of("Angular", "React", "TypeScript", "CSS", "Performance", "State Management"),
            "color", "#06b6d4"
        ),
        Map.of(
            "id", "DSA",
            "title", "DSA & Algorithms",
            "icon", "🧩",
            "description", "Data structures, algorithms, complexity analysis, and problem-solving.",
            "topics", List.of("Arrays", "Trees", "Graphs", "Dynamic Programming", "Sorting", "Binary Search"),
            "color", "#f59e0b"
        ),
        Map.of(
            "id", "DEVOPS",
            "title", "DevOps & Cloud",
            "icon", "☁️",
            "description", "Docker, Kubernetes, CI/CD pipelines, GCP/AWS, and infrastructure as code.",
            "topics", List.of("Docker", "Kubernetes", "CI/CD", "Terraform", "GCP", "Monitoring"),
            "color", "#10b981"
        ),
        Map.of(
            "id", "HR",
            "title", "HR & Behavioural",
            "icon", "🤝",
            "description", "Leadership, conflict resolution, team collaboration, and STAR-method answers.",
            "topics", List.of("Leadership", "Conflict Resolution", "STAR Method", "Teamwork", "Goal Setting"),
            "color", "#ec4899"
        )
    );

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getGroups() {
        return ResponseEntity.ok(GROUPS);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<Map<String, Object>> getGroup(@PathVariable String groupId) {
        return GROUPS.stream()
                .filter(g -> g.get("id").equals(groupId))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
