package com.arnesh.interviewsphere.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "contest_leaderboards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestLeaderboard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "contest_id", nullable = false, unique = true)
    private UUID contestId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selected_resume_candidates", columnDefinition = "jsonb")
    @Builder.Default
    private List<UUID> selectedResumeCandidates = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selected_coding_candidates", columnDefinition = "jsonb")
    @Builder.Default
    private List<UUID> selectedCodingCandidates = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selected_concept_candidates", columnDefinition = "jsonb")
    @Builder.Default
    private List<UUID> selectedConceptCandidates = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "leaderboard", columnDefinition = "jsonb")
    @Builder.Default
    private List<Map<String, Object>> leaderboard = new ArrayList<>();
}