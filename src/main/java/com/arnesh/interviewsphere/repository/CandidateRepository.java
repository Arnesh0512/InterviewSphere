package com.arnesh.interviewsphere.repository;

import com.arnesh.interviewsphere.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arnesh.interviewsphere.annotation.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, UUID> {
    Optional<Candidate> findByEmail(String email);

    Optional<Candidate> findByIdAndEmail(UUID id, String email);

    boolean existsByEmail(String email);
}