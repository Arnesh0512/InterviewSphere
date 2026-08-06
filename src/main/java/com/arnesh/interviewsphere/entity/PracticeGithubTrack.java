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
@Table(name = "practice_github_tracks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticeGithubTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "candidate_id", nullable = false)
    @JsonProperty("candidate_id")
    private UUID candidateId;

    @Column(name = "github_link", nullable = false)
    @JsonProperty("github_link")
    private String githubLink;

    @Column(name = "selected_repo")
    @JsonProperty("selected_repo")
    private String selectedRepo;

    @Column(name = "github_number", nullable = false)
    @JsonProperty("github_number")
    private Integer githubNumber;

    @Column(name = "repo_summary", columnDefinition = "TEXT")
    @JsonProperty("repo_summary")
    private String repoSummary;

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