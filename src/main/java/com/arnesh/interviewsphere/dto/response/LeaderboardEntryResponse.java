package com.arnesh.interviewsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntryResponse {
    private String candidateId;
    private String name;
    private String email;
    private Integer rank;
    private Double percentile;
    private Double score;
    private OffsetDateTime latestSubmission;
}