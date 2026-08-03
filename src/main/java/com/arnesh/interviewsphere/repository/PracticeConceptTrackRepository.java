package com.arnesh.interviewsphere.repository;

import com.arnesh.interviewsphere.entity.PracticeConceptTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arnesh.interviewsphere.annotation.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PracticeConceptTrackRepository extends JpaRepository<PracticeConceptTrack, UUID> {
    Optional<PracticeConceptTrack> findByIdAndCandidateId(UUID id, UUID candidateId);

    List<PracticeConceptTrack> findByCandidateIdOrderByCreatedOnDesc(UUID candidateId);
}