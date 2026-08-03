package com.arnesh.interviewsphere.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveAnswerRequest {

    @NotBlank(message = "Question ID is required")
    private String questionId;

    private String answer;

    private String language;

    @JsonProperty("question_number")
    private Integer questionNumber;
}