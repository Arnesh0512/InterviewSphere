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
@Table(name = "practice_coding_tracks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticeCodingTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "candidate_id", nullable = false)
    @JsonProperty("candidate_id")
    private UUID candidateId;

    @Column(name = "coding_number")
    @JsonProperty("coding_number")
    private Integer codingNumber;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "companies", columnDefinition = "text[]")
    @Builder.Default
    private List<String> company = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "difficulties", columnDefinition = "text[]")
    @Builder.Default
    private List<String> difficulty = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "text[]")
    @Builder.Default
    private List<String> tag = new ArrayList<>();

    @Column(name = "available_ques")
    @JsonProperty("available_ques")
    @Builder.Default
    private Integer availableQues = 0;

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