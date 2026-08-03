package com.arnesh.interviewsphere.util;

import com.arnesh.interviewsphere.dto.request.QuestionScoreEntry;
import com.arnesh.interviewsphere.dto.response.CandidateRankResult;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;

@Component
public class RankingUtil {

    /**
     * Normalizes question raw scores using z-score normalization, accumulates
     * scores, and ranks candidates.
     */
    public List<CandidateRankResult> normalizeAndRank(List<List<QuestionScoreEntry>> candidatesScores) {
        Map<UUID, CandidateAccumulator> results = new HashMap<>();

        for (List<QuestionScoreEntry> questionData : candidatesScores) {
            if (questionData == null || questionData.isEmpty()) {
                continue;
            }

            int n = questionData.size();
            double sum = 0.0;
            for (QuestionScoreEntry entry : questionData) {
                sum += entry.getRawScore();
            }
            double mean = sum / n;

            double varianceSum = 0.0;
            for (QuestionScoreEntry entry : questionData) {
                varianceSum += Math.pow(entry.getRawScore() - mean, 2);
            }
            double variance = varianceSum / n;
            double stdDev = Math.sqrt(variance);

            for (QuestionScoreEntry entry : questionData) {
                UUID candidateId = entry.getCandidateId();
                CandidateAccumulator accum = results.computeIfAbsent(
                        candidateId, k -> new CandidateAccumulator(0.0, null));

                if (stdDev != 0) {
                    double zScore = (entry.getRawScore() - mean) / stdDev;
                    accum.score += zScore;
                }

                OffsetDateTime submittedTime = entry.getSubmittedAt();
                if (accum.latestSubmission == null
                        || (submittedTime != null && submittedTime.isAfter(accum.latestSubmission))) {
                    accum.latestSubmission = submittedTime;
                }
            }
        }

        List<CandidateRankResult> leaderboard = new ArrayList<>();
        for (Map.Entry<UUID, CandidateAccumulator> entry : results.entrySet()) {
            leaderboard.add(CandidateRankResult.builder()
                    .candidateId(entry.getKey())
                    .finalNormalizedScore(entry.getValue().score)
                    .latestSubmission(entry.getValue().latestSubmission)
                    .build());
        }

        return calculateRankAndPercentile(leaderboard);
    }

    /**
     * Combines section-level rank results, calculates final accumulated z-scores,
     * and recalculates global ranks.
     */
    public List<CandidateRankResult> finalizeLeaderboard(List<List<CandidateRankResult>> sectionOutputs) {
        Map<UUID, CandidateAccumulator> cumulative = new HashMap<>();

        for (List<CandidateRankResult> section : sectionOutputs) {
            if (section == null)
                continue;

            for (CandidateRankResult entry : section) {
                UUID candidateId = entry.getCandidateId();
                CandidateAccumulator accum = cumulative.computeIfAbsent(
                        candidateId, k -> new CandidateAccumulator(0.0, null));

                accum.score += entry.getFinalNormalizedScore();

                OffsetDateTime submittedTime = entry.getLatestSubmission();
                if (accum.latestSubmission == null
                        || (submittedTime != null && submittedTime.isAfter(accum.latestSubmission))) {
                    accum.latestSubmission = submittedTime;
                }
            }
        }

        List<CandidateRankResult> leaderboard = new ArrayList<>();
        for (Map.Entry<UUID, CandidateAccumulator> entry : cumulative.entrySet()) {
            leaderboard.add(CandidateRankResult.builder()
                    .candidateId(entry.getKey())
                    .finalNormalizedScore(entry.getValue().score)
                    .latestSubmission(entry.getValue().latestSubmission)
                    .build());
        }

        return calculateRankAndPercentile(leaderboard);
    }

    /**
     * Helper method to sort candidates, assign ranks (handling ties), and compute
     * percentiles.
     */
    private List<CandidateRankResult> calculateRankAndPercentile(List<CandidateRankResult> leaderboard) {
        // Sort: 1) Higher final score first, 2) Tie-break with earlier latest
        // submission time
        leaderboard.sort((a, b) -> {
            int scoreCompare = Double.compare(b.getFinalNormalizedScore(), a.getFinalNormalizedScore());
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            if (a.getLatestSubmission() == null && b.getLatestSubmission() == null)
                return 0;
            if (a.getLatestSubmission() == null)
                return 1;
            if (b.getLatestSubmission() == null)
                return -1;
            return a.getLatestSubmission().compareTo(b.getLatestSubmission());
        });

        int totalCandidates = leaderboard.size();
        int currentRank = 1;

        for (int i = 0; i < totalCandidates; i++) {
            CandidateRankResult current = leaderboard.get(i);

            if (i > 0) {
                CandidateRankResult prev = leaderboard.get(i - 1);
                boolean sameScore = Math.abs(current.getFinalNormalizedScore() - prev.getFinalNormalizedScore()) < 1e-9;
                boolean sameTime = Objects.equals(current.getLatestSubmission(), prev.getLatestSubmission());

                if (sameScore && sameTime) {
                    current.setRank(prev.getRank());
                } else {
                    current.setRank(currentRank);
                }
            } else {
                current.setRank(currentRank);
            }
            currentRank++;

            // Percentile calculation
            if (totalCandidates == 1) {
                current.setPercentile(100.0);
            } else {
                double percentile = ((double) (totalCandidates - current.getRank()) / (totalCandidates - 1)) * 100.0;
                current.setPercentile(percentile);
            }
        }

        return leaderboard;
    }

    private static class CandidateAccumulator {
        double score;
        OffsetDateTime latestSubmission;

        CandidateAccumulator(double score, OffsetDateTime latestSubmission) {
            this.score = score;
            this.latestSubmission = latestSubmission;
        }
    }
}