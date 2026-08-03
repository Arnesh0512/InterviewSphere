package com.arnesh.interviewsphere.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.arnesh.interviewsphere.constant.Company;
import com.arnesh.interviewsphere.constant.Role;
import com.arnesh.interviewsphere.constant.Skill;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_created_id")
    private Admin adminCreated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "contest_skills", joinColumns = @JoinColumn(name = "contest_id"))
    @Column(name = "skill")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @Column(name = "last_date_to_register", nullable = false)
    private OffsetDateTime lastDateToRegister;

    @Column(name = "contest_start", nullable = false)
    private OffsetDateTime contestStart;

    @Column(name = "contest_end", nullable = false)
    private OffsetDateTime contestEnd;

    @Column(name = "leaderboard_declare_time", nullable = false)
    private OffsetDateTime leaderboardDeclareTime;

    // Stored as JSONB to cleanly group round timings
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resume_round", columnDefinition = "jsonb", nullable = false)
    private RoundTimeDto resumeRound;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "coding_round", columnDefinition = "jsonb", nullable = false)
    private RoundTimeDto codingRound;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "concept_round", columnDefinition = "jsonb", nullable = false)
    private RoundTimeDto conceptRound;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hr_round", columnDefinition = "jsonb", nullable = false)
    private RoundTimeDto hrRound;

    @Column(name = "resume_questions_count", nullable = false)
    private Integer resumeQuestionsCount;

    @Column(name = "coding_questions_count", nullable = false)
    private Integer codingQuestionsCount;

    @Column(name = "concept_questions_count", nullable = false)
    private Integer conceptQuestionsCount;

    @Column(name = "hr_questions_count", nullable = false)
    private Integer hrQuestionsCount;

    @Column(name = "candidate_capacity", nullable = false)
    private Integer candidateCapacity;

    @Column(name = "candidate_count")
    @Builder.Default
    private Integer candidateCount = 0;

    @Column(name = "selected_resume", nullable = false)
    private Integer selectedResume;

    @Column(name = "selected_coding", nullable = false)
    private Integer selectedCoding;

    @Column(name = "selected_concept", nullable = false)
    private Integer selectedConcept;

    @Column(name = "selected_hr", nullable = false)
    private Integer selectedHr;

    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    private OffsetDateTime createdOn;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoundTimeDto {
        private OffsetDateTime start;
        private OffsetDateTime end;
        private Integer duration; // in minutes
        private OffsetDateTime result;
        private Object questions;
    }
}