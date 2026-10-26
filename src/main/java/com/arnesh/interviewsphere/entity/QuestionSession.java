package com.arnesh.interviewsphere.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "question_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "coding_track_id")
    private UUID codingTrackId;

    @Column(name = "concept_track_id")
    private UUID conceptTrackId;

    @Column(name = "github_track_id")
    private UUID githubTrackId;

    @Column(name = "resume_track_id")
    private UUID resumeTrackId;

    @Column(name = "session_number")
    private Integer sessionNumber;

    @Column(nullable = false)
    private String status; // "active" or "passive"

    @Column(nullable = false)
    private Integer time; // Session duration in minutes

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private OffsetDateTime timestamp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "question_bank", columnDefinition = "jsonb")
    @Builder.Default
    private List<Question> questionBank = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Question {
        @JsonProperty("question_id")
        private Integer questionId;

        private String question;
        private String answer;
        private String language;
        private String feedback;
        private Double score;
    }

    @Column(name = "overall_feedback", columnDefinition = "TEXT")
    @JsonProperty("overall_feedback")
    private String overallFeedback;

    @Column(name = "overall_score")
    @JsonProperty("overall_score")
    private Double overallScore;

    @Column(name = "submitted_at_frontend")
    private OffsetDateTime submittedAtFrontend;

    @Column(name = "submitted_at_backend")
    private OffsetDateTime submittedAtBackend;
}