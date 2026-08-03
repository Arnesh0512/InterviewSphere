package com.arnesh.interviewsphere.repository;

import com.arnesh.interviewsphere.entity.QuestionSession;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arnesh.interviewsphere.annotation.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionSessionRepository extends JpaRepository<QuestionSession, UUID> {

        Optional<QuestionSession> findByIdAndCodingTrackId(UUID id, UUID codingTrackId);
        Optional<QuestionSession> findByIdAndConceptTrackId(UUID id, UUID conceptTrackId);
        Optional<QuestionSession> findByIdAndGithubTrackId(UUID id, UUID githubTrackId);
        Optional<QuestionSession> findByIdAndResumeTrackId(UUID id, UUID resumeTrackId);

        
        List<QuestionSession> findByCodingTrackId(UUID codingTrackId);
        List<QuestionSession> findByConceptTrackId(UUID conceptTrackId);
        List<QuestionSession> findByGithubTrackId(UUID githubTrackId);
        List<QuestionSession> findByResumeTrackId(UUID resumeTrackId);


        List<QuestionSession> findByCodingTrackIdAndStatusOrderByTimestampDesc(UUID codingTrackId, String status);
        List<QuestionSession> findByCodingTrackIdAndStatusAndSessionNumberOrderByTimestampDesc(UUID codingTrackId,  String status, Integer sessionNumber);
        List<QuestionSession> findByCodingTrackIdOrderByTimestampAsc(UUID codingTrackId);

        List<QuestionSession> findByConceptTrackIdAndStatusOrderByTimestampDesc(UUID conceptTrackId, String status);
        List<QuestionSession> findByConceptTrackIdAndStatusAndSessionNumberOrderByTimestampDesc(UUID conceptTrackId, String status, Integer sessionNumber);
        List<QuestionSession> findByConceptTrackIdOrderByTimestampAsc(UUID conceptTrackId);


        List<QuestionSession> findByGithubTrackIdAndStatusOrderByTimestampDesc(UUID githubTrackId, String status);
        List<QuestionSession> findByGithubTrackIdAndStatusAndSessionNumberOrderByTimestampDesc(UUID githubTrackId,  String status, Integer sessionNumber);
        List<QuestionSession> findByGithubTrackIdOrderByTimestampAsc(UUID githubTrackId);


        List<QuestionSession> findByResumeTrackIdAndStatusOrderByTimestampDesc(UUID resumeTrackId, String status);
        List<QuestionSession> findByResumeTrackIdAndStatusAndSessionNumberOrderByTimestampDesc(UUID resumeTrackId,  String status, Integer sessionNumber);
        List<QuestionSession> findByResumeTrackIdOrderByTimestampAsc(UUID resumeTrackId);


        long countByGithubTrackIdAndSessionNumber(UUID githubTrackId, Integer sessionNumber);
        long countByResumeTrackIdAndSessionNumber(UUID resumeTrackId, Integer sessionNumber);
        long countByConceptTrackIdAndSessionNumber(UUID conceptTrackId, Integer sessionNumber);
        long countByCodingTrackIdAndSessionNumber(UUID codingTrackId, Integer sessionNumber);


        void deleteByGithubTrackId(UUID githubTrackId);
        void deleteByResumeTrackId(UUID resumeTrackId);
        void deleteByConceptTrackId(UUID conceptTrackId);
        void deleteByCodingTrackId(UUID codingTrackId);
}