package com.arnesh.interviewsphere.repository;

import com.arnesh.interviewsphere.entity.PracticeResumeTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arnesh.interviewsphere.annotation.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PracticeResumeTrackRepository extends JpaRepository<PracticeResumeTrack, UUID> {
    List<PracticeResumeTrack> findByCandidateIdOrderByCreatedOnDesc(UUID candidateId);

    Optional<PracticeResumeTrack> findByIdAndCandidateId(UUID id, UUID candidateId);
}