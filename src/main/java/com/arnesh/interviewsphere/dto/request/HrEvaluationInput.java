package com.arnesh.interviewsphere.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrEvaluationInput {
    private Integer questionId;
    private String question;
    private String transcript;
    private List<Object> segmentedData;
}