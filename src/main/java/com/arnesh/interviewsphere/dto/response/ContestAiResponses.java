package com.arnesh.interviewsphere.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ContestAiResponses {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SummaryResponse {
        private String summary;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoreResult {
        @JsonProperty("question_id")
        private Integer questionId;
        private String feedback;
        private Double score;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContestEvaluationResponse {
        private List<ScoreResult> results;

        @JsonProperty("overall_feedback")
        private String overallFeedback;
    }
}