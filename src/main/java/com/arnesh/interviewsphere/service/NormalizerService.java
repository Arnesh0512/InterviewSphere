package com.arnesh.interviewsphere.service;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class NormalizerService {

    public record CandidateQuestionScore(
            String candidateId,
            double rawScore,
            OffsetDateTime submittedAt
    ) {}

    public record RankedCandidate(
            String candidateId,
            double finalNormalizedScore,
            OffsetDateTime latestSubmission,
            int rank,
            double percentile
    ) {}

    private static class CandidateAccumulator {
        double finalNormalizedScore = 0.0;
        OffsetDateTime latestSubmission = null;
    }

    /**
     * Step 1 - 4: Z-score Normalization, Accumulation, Tie-break Sorting, Ranking, and Percentiles
     */
    public List<RankedCandidate> normalizeAndRank(List<List<CandidateQuestionScore>> candidatesScores) {
        if (candidatesScores == null || candidatesScores.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, CandidateAccumulator> results = new HashMap<>();

        // Step 1: Normalize all questions and accumulate final z-score
        for (List<CandidateQuestionScore> questionData : candidatesScores) {
            if (questionData == null || questionData.isEmpty()) {
                continue;
            }

            int n = questionData.size();
            double sumRaw = 0.0;
            for (CandidateQuestionScore entry : questionData) {
                sumRaw += entry.rawScore();
            }

            double mean = sumRaw / n;
            double varianceSum = 0.0;
            for (CandidateQuestionScore entry : questionData) {
                double diff = entry.rawScore() - mean;
                varianceSum += diff * diff;
            }
            double stdDev = Math.sqrt(varianceSum / n);

            // If all same score -> stdDev = 0 (z = 0)
            if (stdDev == 0.0) {
                for (CandidateQuestionScore entry : questionData) {
                    String candidateId = entry.candidateId();
                    CandidateAccumulator acc = results.computeIfAbsent(candidateId, k -> new CandidateAccumulator());
                    
                    OffsetDateTime submittedTime = entry.submittedAt();
                    if (acc.latestSubmission == null || (submittedTime != null && submittedTime.isAfter(acc.latestSubmission))) {
                        acc.latestSubmission = submittedTime;
                    }
                }
                continue;
            }

            // Compute z-score and accumulate
            for (CandidateQuestionScore entry : questionData) {
                String candidateId = entry.candidateId();
                double rawScore = entry.rawScore();
                double zScore = (rawScore - mean) / stdDev;

                CandidateAccumulator acc = results.computeIfAbsent(candidateId, k -> new CandidateAccumulator());
                acc.finalNormalizedScore += zScore;

                OffsetDateTime submittedTime = entry.submittedAt();
                if (acc.latestSubmission == null || (submittedTime != null && submittedTime.isAfter(acc.latestSubmission))) {
                    acc.latestSubmission = submittedTime;
                }
            }
        }

        // Step 2: Create list for ranking
        List<RankedCandidateBuilder> leaderboard = new ArrayList<>();
        for (Map.Entry<String, CandidateAccumulator> entry : results.entrySet()) {
            leaderboard.add(new RankedCandidateBuilder(
                    entry.getKey(),
                    entry.getValue().finalNormalizedScore,
                    entry.getValue().latestSubmission
            ));
        }

        // Step 3: Sort leaderboard
        // 1) Higher final score first (-final_normalized_score)
        // 2) Tie -> earlier submission wins
        leaderboard.sort((a, b) -> {
            int scoreCompare = Double.compare(b.finalNormalizedScore, a.finalNormalizedScore);
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            if (a.latestSubmission == null && b.latestSubmission == null) return 0;
            if (a.latestSubmission == null) return 1;
            if (b.latestSubmission == null) return -1;
            return a.latestSubmission.compareTo(b.latestSubmission);
        });

        // Step 4: Assign rank and percentile
        int totalCandidates = leaderboard.size();
        int currentRank = 1;
        List<RankedCandidate> finalRanked = new ArrayList<>();

        for (int i = 0; i < totalCandidates; i++) {
            RankedCandidateBuilder current = leaderboard.get(i);
            int rankToAssign;

            if (i > 0) {
                RankedCandidateBuilder prev = leaderboard.get(i - 1);
                boolean sameScore = Math.abs(current.finalNormalizedScore - prev.finalNormalizedScore) < 1e-9;
                boolean sameTime = Objects.equals(current.latestSubmission, prev.latestSubmission);

                if (sameScore && sameTime) {
                    rankToAssign = finalRanked.get(i - 1).rank();
                } else {
                    rankToAssign = currentRank;
                }
            } else {
                rankToAssign = currentRank;
            }

            currentRank++;

            // Percentile calculation
            double percentile;
            if (totalCandidates == 1) {
                percentile = 100.0;
            } else {
                percentile = ((double) (totalCandidates - rankToAssign) / (totalCandidates - 1)) * 100.0;
            }

            finalRanked.add(new RankedCandidate(
                    current.candidateId,
                    current.finalNormalizedScore,
                    current.latestSubmission,
                    rankToAssign,
                    percentile
            ));
        }

        return finalRanked;
    }

    /**
     * Finalizes overall contest leaderboard by summing round normalized scores across sections.
     */
    public List<RankedCandidate> finalizeLeaderboard(List<List<RankedCandidate>> sectionOutputs) {
        if (sectionOutputs == null || sectionOutputs.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, CandidateAccumulator> cumulative = new HashMap<>();

        // Step 1: Accumulate final_normalized_score & track latest submission per candidate
        for (List<RankedCandidate> section : sectionOutputs) {
            if (section == null) continue;

            for (RankedCandidate entry : section) {
                String candidateId = entry.candidateId();
                CandidateAccumulator acc = cumulative.computeIfAbsent(candidateId, k -> new CandidateAccumulator());

                acc.finalNormalizedScore += entry.finalNormalizedScore();

                OffsetDateTime submittedTime = entry.latestSubmission();
                if (acc.latestSubmission == null || (submittedTime != null && submittedTime.isAfter(acc.latestSubmission))) {
                    acc.latestSubmission = submittedTime;
                }
            }
        }

        // Step 2: Create list for ranking
        List<RankedCandidateBuilder> leaderboard = new ArrayList<>();
        for (Map.Entry<String, CandidateAccumulator> entry : cumulative.entrySet()) {
            leaderboard.add(new RankedCandidateBuilder(
                    entry.getKey(),
                    entry.getValue().finalNormalizedScore,
                    entry.getValue().latestSubmission
            ));
        }

        // Step 3: Sort leaderboard
        leaderboard.sort((a, b) -> {
            int scoreCompare = Double.compare(b.finalNormalizedScore, a.finalNormalizedScore);
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            if (a.latestSubmission == null && b.latestSubmission == null) return 0;
            if (a.latestSubmission == null) return 1;
            if (b.latestSubmission == null) return -1;
            return a.latestSubmission.compareTo(b.latestSubmission);
        });

        // Step 4: Assign rank and percentile
        int totalCandidates = leaderboard.size();
        int currentRank = 1;
        List<RankedCandidate> finalRanked = new ArrayList<>();

        for (int i = 0; i < totalCandidates; i++) {
            RankedCandidateBuilder current = leaderboard.get(i);
            int rankToAssign;

            if (i > 0) {
                RankedCandidateBuilder prev = leaderboard.get(i - 1);
                boolean sameScore = Math.abs(current.finalNormalizedScore - prev.finalNormalizedScore) < 1e-9;
                boolean sameTime = Objects.equals(current.latestSubmission, prev.latestSubmission);

                if (sameScore && sameTime) {
                    rankToAssign = finalRanked.get(i - 1).rank();
                } else {
                    rankToAssign = currentRank;
                }
            } else {
                rankToAssign = currentRank;
            }

            currentRank++;

            double percentile;
            if (totalCandidates == 1) {
                percentile = 100.0;
            } else {
                percentile = ((double) (totalCandidates - rankToAssign) / (totalCandidates - 1)) * 100.0;
            }

            finalRanked.add(new RankedCandidate(
                    current.candidateId,
                    current.finalNormalizedScore,
                    current.latestSubmission,
                    rankToAssign,
                    percentile
            ));
        }

        return finalRanked;
    }

    private static class RankedCandidateBuilder {
        String candidateId;
        double finalNormalizedScore;
        OffsetDateTime latestSubmission;

        RankedCandidateBuilder(String candidateId, double finalNormalizedScore, OffsetDateTime latestSubmission) {
            this.candidateId = candidateId;
            this.finalNormalizedScore = finalNormalizedScore;
            this.latestSubmission = latestSubmission;
        }
    }
}