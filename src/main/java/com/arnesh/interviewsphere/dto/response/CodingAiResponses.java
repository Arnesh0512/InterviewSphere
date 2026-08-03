package com.arnesh.interviewsphere.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class CodingAiResponses {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionFeedback {
        @JsonProperty("question_id")
        private Integer questionId;

        private String feedback;
        private Double score;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CodingEvaluationResponse {
        @JsonProperty("feedback_per_question")
        private List<QuestionFeedback> feedbackPerQuestion;

        @JsonProperty("overall_feedback")
        private String overallFeedback;

        @JsonProperty("overall_score")
        private Double overallScore;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CombinedFeedbackDetails {
        private String improvement;
        private String weaknesses;
        private String strengths;
        private String recommendations;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CombinedFeedbackResponse {
        private CombinedFeedbackDetails feedback;
    }
}