package com.interviewagent.backend.repositories;

import com.interviewagent.backend.models.Folder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends MongoRepository<Folder, String> {
    List<Folder> findByProjectId(String projectId);
    List<Folder> findByParentFolderId(String parentFolderId);
    List<Folder> findByProjectIdAndParentFolderIdIsNull(String projectId);
}
