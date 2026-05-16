package com.interviewagent.backend.repositories;

import com.interviewagent.backend.models.Resource;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends MongoRepository<Resource, String> {
    List<Resource> findByUserId(String userId);
    List<Resource> findByFolderId(String folderId);
    List<Resource> findByProjectId(String projectId);
}

