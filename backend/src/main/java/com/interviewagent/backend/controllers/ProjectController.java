package com.interviewagent.backend.controllers;

import com.interviewagent.backend.models.Project;
import com.interviewagent.backend.repositories.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;

    @GetMapping
    public ResponseEntity<List<Project>> getProjects(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(projectRepository.findByUserId(email));
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Map<String, String> body, Authentication auth) {
        Project project = Project.builder()
                .name(body.get("name"))
                .description(body.getOrDefault("description", ""))
                .userId(auth.getName())
                .createdAt(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(projectRepository.save(project));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable String id) {
        projectRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
