package com.interviewagent.backend.controllers;

import com.interviewagent.backend.models.Folder;
import com.interviewagent.backend.repositories.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderRepository folderRepository;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Folder>> getFoldersByProject(@PathVariable String projectId) {
        return ResponseEntity.ok(folderRepository.findByProjectIdAndParentFolderIdIsNull(projectId));
    }

    @GetMapping("/{folderId}/children")
    public ResponseEntity<List<Folder>> getChildFolders(@PathVariable String folderId) {
        return ResponseEntity.ok(folderRepository.findByParentFolderId(folderId));
    }

    @PostMapping
    public ResponseEntity<Folder> createFolder(@RequestBody Map<String, String> body, Authentication auth) {
        Folder folder = Folder.builder()
                .name(body.get("name"))
                .projectId(body.get("projectId"))
                .parentFolderId(body.get("parentFolderId")) // may be null for root
                .userId(auth.getName())
                .createdAt(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(folderRepository.save(folder));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFolder(@PathVariable String id) {
        folderRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
