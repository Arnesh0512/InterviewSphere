package com.arnesh.interviewsphere.repository;

import com.arnesh.interviewsphere.entity.PracticeGithubTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arnesh.interviewsphere.annotation.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PracticeGithubTrackRepository extends JpaRepository<PracticeGithubTrack, UUID> {
    Optional<PracticeGithubTrack> findByIdAndCandidateId(UUID id, UUID candidateId);

    List<PracticeGithubTrack> findByCandidateIdOrderByCreatedOnDesc(UUID candidateId);
}