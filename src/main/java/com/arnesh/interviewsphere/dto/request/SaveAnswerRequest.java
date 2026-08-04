package com.arnesh.interviewsphere.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveAnswerRequest {

    @NotNull(message = "Question ID is required")
    private Integer questionId;

    private String answer;

    private String language;

}