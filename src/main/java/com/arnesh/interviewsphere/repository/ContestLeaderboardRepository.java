package com.arnesh.interviewsphere.repository;

import com.arnesh.interviewsphere.entity.ContestLeaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arnesh.interviewsphere.annotation.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContestLeaderboardRepository extends JpaRepository<ContestLeaderboard, UUID> {
    Optional<ContestLeaderboard> findByContestId(UUID contestId);
    void deleteByContestId(UUID contestId);
}