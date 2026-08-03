package com.arnesh.interviewsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionSessionResponse {

    private String sessionId;
    private Integer sessionNumber;
    private String status; // "active" or "passive"
    private Integer time; // Session duration in minutes
    private OffsetDateTime timestamp;
    private List<Map<String, Object>> questionBank;
}