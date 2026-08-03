package com.arnesh.interviewsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateRankResult {
    private UUID candidateId;
    private double finalNormalizedScore;
    private OffsetDateTime latestSubmission;
    private Integer rank;
    private Double percentile;
}