package com.arnesh.interviewsphere.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodingEvaluationInput {
    private String questionId;
    private String problemDescription;
    private String answer;
    private String language;
}