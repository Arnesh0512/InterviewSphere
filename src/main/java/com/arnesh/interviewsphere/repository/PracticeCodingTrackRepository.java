package com.arnesh.interviewsphere.repository;

import com.arnesh.interviewsphere.entity.PracticeCodingTrack;

import org.springframework.data.jpa.repository.JpaRepository;
import com.arnesh.interviewsphere.annotation.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PracticeCodingTrackRepository extends JpaRepository<PracticeCodingTrack, UUID> {
    Optional<PracticeCodingTrack> findByIdAndCandidateId(UUID id, UUID candidateId);

    List<PracticeCodingTrack> findByCandidateId(UUID candidateId);

    List<PracticeCodingTrack> findByCandidateIdOrderByCreatedOnDesc(UUID candidateId);
}
