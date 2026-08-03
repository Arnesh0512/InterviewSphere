package com.arnesh.interviewsphere.repository;

import com.arnesh.interviewsphere.constant.Company;
import com.arnesh.interviewsphere.constant.Role;
import com.arnesh.interviewsphere.constant.Skill;
import com.arnesh.interviewsphere.entity.Contest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.arnesh.interviewsphere.annotation.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ContestRepository extends JpaRepository<Contest, UUID> {

    List<Contest> findByAdminCreatedId(UUID adminId);

    @Query(value = """
        SELECT DISTINCT c.* FROM contests c
        LEFT JOIN contest_skills cs ON c.id = cs.contest_id
        WHERE c.role IN (:roles) AND cs.skill IN (:skills)
        ORDER BY c.created_on DESC
        """, nativeQuery = true)
    List<Contest> findAvailableContests(
            @Param("roles") List<String> roles,
            @Param("skills") List<String> skills
    );

    @Query(value = """
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM contests c
        JOIN contest_skills cs ON c.id = cs.contest_id
        WHERE c.company = :company 
        AND c.role = :role
        AND c.candidate_capacity = :capacity
        AND c.last_date_to_register = :lastReg
        AND c.contest_start = :cStart
        AND cs.skill = ANY(:skills)
        AND (c.resume_round->>'start')::timestamptz = :resStart
        AND (c.coding_round->>'start')::timestamptz = :codStart
        AND (c.concept_round->>'start')::timestamptz = :conStart
        AND (c.hr_round->>'start')::timestamptz = :hrStart
    """, nativeQuery = true)
    boolean existsMatchingContest(
            @Param("company") Company company,
            @Param("role") Role role,
            @Param("skills") List<Skill> skills,
            @Param("capacity") Integer capacity,
            @Param("lastReg") OffsetDateTime lastReg,
            @Param("cStart") OffsetDateTime cStart,
            @Param("resStart") OffsetDateTime resStart,
            @Param("codStart") OffsetDateTime codStart,
            @Param("conStart") OffsetDateTime conStart,
            @Param("hrStart") OffsetDateTime hrStart
    );
}