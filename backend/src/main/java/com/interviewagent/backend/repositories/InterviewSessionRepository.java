package com.interviewagent.backend.repositories;

import com.interviewagent.backend.models.InterviewSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewSessionRepository extends MongoRepository<InterviewSession, String> {
    List<InterviewSession> findByUserId(String userId);
    List<InterviewSession> findByUserIdOrderByStartedAtDesc(String userId);
}
