package com.arnesh.interviewsphere.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "practice_concept_tracks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticeConceptTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "candidate_id", nullable = false)
    @JsonProperty("candidate_id")
    private UUID candidateId;

    @Column(name = "concept_number")
    @JsonProperty("concept_number")
    private Integer conceptNumber;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "topics", columnDefinition = "text[]")
    @Builder.Default
    private List<String> topics = new ArrayList<>();

    @Column(name = "total_sessions")
    @JsonProperty("total_sessions")
    @Builder.Default
    private Integer totalSessions = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "combined_feedback", columnDefinition = "jsonb")
    @Builder.Default
    private List<Map<String, Object>> combinedFeedback = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    @JsonProperty("created_on")
    private OffsetDateTime createdOn;
}