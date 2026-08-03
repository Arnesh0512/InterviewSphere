package com.arnesh.interviewsphere.repository;

import com.arnesh.interviewsphere.entity.ContestCandidateResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arnesh.interviewsphere.annotation.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContestCandidateResponseRepository extends JpaRepository<ContestCandidateResponse, UUID> {
    
    Optional<ContestCandidateResponse> findByContestIdAndCandidateId(UUID contestId, UUID candidateId);

    List<ContestCandidateResponse> findByContestIdAndCandidateIdIn(UUID contestId, List<UUID> candidateIds);

    List<ContestCandidateResponse> findByContestId(UUID contestId);

    void deleteByContestIdAndCandidateId(UUID contestId, UUID candidateId);

    void deleteByContestId(UUID contestId);
}