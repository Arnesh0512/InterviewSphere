package com.arnesh.interviewsphere.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "leetcode_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeetCodeQuestion {

    @Id
    @Column(name = "question_id")
    @JsonProperty("question_id")
    private String questionId;

    @Column(name = "task_name", nullable = false)
    @JsonProperty("task_name")
    private String taskName;

    @Column(nullable = false)
    @JsonProperty("difficulty")
    private String difficulty;

    @Column(name = "problem_description", columnDefinition = "TEXT", nullable = false)
    @JsonProperty("problem_description")
    private String problemDescription;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "text[]")
    @JsonProperty("tags")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "companies", columnDefinition = "text[]")
    @JsonProperty("companies")
    @Builder.Default
    private List<String> companies = new ArrayList<>();
}