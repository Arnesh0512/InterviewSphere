package com.arnesh.interviewsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class AdminAiResponses {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationResponse {
        private Boolean valid;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionListResponse {
        private List<String> questions;
    }
}