package com.arnesh.interviewsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreviousResumeSessionResponse {
    private Map<String, Map<String, String>> sessionDict;
    private Map<String, Map<String, Object>> sessionsUsed;
}