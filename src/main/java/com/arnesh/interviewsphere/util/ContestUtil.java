package com.arnesh.interviewsphere.util;

import com.arnesh.interviewsphere.dto.response.LeaderboardEntryResponse;
import com.arnesh.interviewsphere.entity.Candidate;
import com.arnesh.interviewsphere.entity.ContestCandidateResponse;
import com.arnesh.interviewsphere.repository.CandidateRepository;
import com.arnesh.interviewsphere.repository.ContestCandidateResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContestUtil {

    private final ContestCandidateResponseRepository contestCandidateResponseRepository;
    private final CandidateRepository candidateRepository;

    
    public void fakeSubmitCandidateCoding(UUID contestId, List<UUID> fakeSubmitIds) {
        if (fakeSubmitIds == null || fakeSubmitIds.isEmpty()) {
            return;
        }

        List<ContestCandidateResponse> responses = contestCandidateResponseRepository
                .findByContestIdAndCandidateIdIn(contestId, fakeSubmitIds);

        for (ContestCandidateResponse response : responses) {
            if (response.getCoding() != null) {
                response.getCoding().put("submittedAt", response.getCreatedOn());
            }
            contestCandidateResponseRepository.save(response);
        }
    }

    
    public List<LeaderboardEntryResponse> formatLeaderboard(List<ContestCandidateResponse> entries) {
        List<UUID> candidateIds = entries.stream()
                .map(r -> r.getCandidateId())
                .distinct()
                .collect(Collectors.toList());

        Map<UUID, Candidate> candidateMap = candidateRepository.findAllById(candidateIds).stream()
                .collect(Collectors.toMap(c -> c.getId(), Function.identity()));

        return entries.stream().map(entry -> {
            Candidate candidate = candidateMap.get(entry.getCandidateId());
            return LeaderboardEntryResponse.builder()
                    .candidateId(entry.getCandidateId().toString())
                    .name(candidate != null ? candidate.getFullName() : null)
                    .email(candidate != null ? candidate.getEmail() : null)
                    .build();
        }).collect(Collectors.toList());
    }
}