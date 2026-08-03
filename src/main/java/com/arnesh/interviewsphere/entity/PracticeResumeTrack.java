package com.arnesh.interviewsphere.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "practice_resume_tracks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticeResumeTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "candidate_id", nullable = false)
    @JsonProperty("candidate_id")
    private UUID candidateId;

    @Column(name = "resume_number", nullable = false)
    @JsonProperty("resume_number")
    private Integer resumeNumber;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "file_url")
    @JsonProperty("file_url")
    private String fileUrl;

    private String filename;

    @Column(name = "created_on")
    @JsonProperty("created_on")
    private OffsetDateTime createdOn;

    @Column(name = "total_sessions")
    @JsonProperty("total_sessions")
    @Builder.Default
    private Integer totalSessions = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "combined_feedback", columnDefinition = "jsonb")
    @Builder.Default
    private List<Map<String, Object>> combinedFeedback = new ArrayList<>();
}