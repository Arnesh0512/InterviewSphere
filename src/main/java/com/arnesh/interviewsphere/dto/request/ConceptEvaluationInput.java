package com.arnesh.interviewsphere.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptEvaluationInput {
    private Integer questionId;
    private String question;
    private String answer;
}