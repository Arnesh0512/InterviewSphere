package com.arnesh.interviewsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardResponse {

    private String contestId;
    private List<LeaderboardEntryResponse> leaderboard;
    private Integer totalCandidates;
}