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
public class GithubRepoDetailsResponse {
    private String repoName;
    private String description;
    private Map<String, Object> languages;
    private String readme;
}