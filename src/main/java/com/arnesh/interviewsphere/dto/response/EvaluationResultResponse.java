package com.arnesh.interviewsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationResultResponse {

    private List<QuestionEvaluation> results;
    private String overallFeedback;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionEvaluation {
        private String questionId;
        private Double score;
        private String feedback;
    }
}