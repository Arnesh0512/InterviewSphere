package com.arnesh.interviewsphere.verification;

import com.arnesh.interviewsphere.entity.Candidate;
import com.arnesh.interviewsphere.entity.Contest;
import com.arnesh.interviewsphere.entity.ContestCandidateResponse;
import com.arnesh.interviewsphere.entity.ContestLeaderboard;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.ContestCandidateResponseRepository;
import com.arnesh.interviewsphere.repository.ContestLeaderboardRepository;
import com.arnesh.interviewsphere.repository.ContestRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContestVerificationService {

    private final ContestRepository contestRepository;
    private final ContestCandidateResponseRepository contestCandidateResponseRepository;
    private final ContestLeaderboardRepository contestLeaderboardRepository;

    @Data
    @Builder
    public static class VerifiedContestResult {
        private Contest contest;
        private UUID contestId;
    }

    // ==========================================
    // 1. CONTEST & ELIGIBILITY VERIFICATION
    // ==========================================

    public VerifiedContestResult verifyContestId(String contestIdStr) {
        UUID contestId = parseUuid(contestIdStr, "Invalid contest_id", HttpStatus.BAD_REQUEST);

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new CustomException("Contest not found", HttpStatus.NOT_FOUND));

        return VerifiedContestResult.builder()
                .contest(contest)
                .contestId(contestId)
                .build();
    }

    public void verifyCandidateEligibility(OffsetDateTime currentTime, Candidate candidate, Contest contest) {
        if (currentTime.isAfter(contest.getLastDateToRegister())) {
            throw new CustomException("Registration closed", HttpStatus.BAD_REQUEST);
        }

        int count = contest.getCandidateCount() != null ? contest.getCandidateCount() : 0;
        int capacity = contest.getCandidateCapacity() != null ? contest.getCandidateCapacity() : 0;

        if (count >= capacity) {
            throw new CustomException("Contest capacity full", HttpStatus.BAD_REQUEST);
        }

        List<String> candidateRoles = candidate.getRoles() != null
                ? candidate.getRoles().stream().map(s -> s.name()).toList()
                : List.of();

        if (!candidateRoles.contains(contest.getRole().name())) {
            throw new CustomException("Candidate role mismatch", HttpStatus.FORBIDDEN);
        }

        List<String> candidateSkills = candidate.getSkills() != null
                ? candidate.getSkills().stream().map(s -> s.name()).toList()
                : List.of();
        List<String> contestSkills = contest.getSkills() != null
                ? contest.getSkills().stream().map(s -> s.name()).toList()
                : List.of();

        boolean hasMatchingSkill = candidateSkills.stream().anyMatch(contestSkills::contains);
        if (!hasMatchingSkill) {
            throw new CustomException("Candidate skills mismatch", HttpStatus.FORBIDDEN);
        }
    }

    public ContestCandidateResponse verifyContestRegistry(Candidate candidate, Contest contest, String type) {
        var existing = contestCandidateResponseRepository
                .findByContestIdAndCandidateId(contest.getId(), candidate.getId());

        boolean isRegistered = existing.isPresent();

        if ("N".equalsIgnoreCase(type)) {
            if (isRegistered) {
                throw new CustomException("Already registered", HttpStatus.BAD_REQUEST);
            }
            return null;
        }

        if ("Y".equalsIgnoreCase(type)) {
            if (!isRegistered) {
                throw new CustomException("Candidate not registered for contest", HttpStatus.BAD_REQUEST);
            }
            return existing.get();
        }

        return null;
    }

    public void verifyUnregisterTime(OffsetDateTime timestamp, Contest contest) {
        if (timestamp.isAfter(contest.getLastDateToRegister())) {
            throw new CustomException("Cannot unregister after registration deadline", HttpStatus.BAD_REQUEST);
        }
    }

    public OffsetDateTime verifyTimestamp(OffsetDateTime frontendTimestamp, OffsetDateTime backendTimestamp) {
        if (frontendTimestamp == null) {
            throw new CustomException("Invalid frontend timestamp format", HttpStatus.BAD_REQUEST);
        }

        OffsetDateTime normalizedFrontend = frontendTimestamp.withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime normalizedBackend = backendTimestamp.withOffsetSameInstant(ZoneOffset.UTC);

        long timeDiffSeconds = Math.abs(Duration.between(normalizedBackend, normalizedFrontend).getSeconds());

        if (timeDiffSeconds > 120) {
            throw new CustomException("Submission time mismatch exceeds 2 minutes", HttpStatus.BAD_REQUEST);
        }

        return normalizedFrontend;
    }

    // ==========================================
    // 2. ROUND TIMING VERIFICATIONS
    // ==========================================

    public void verifyResumeTimeOpen(OffsetDateTime timestamp, Contest contest) {
        OffsetDateTime start = contest.getResumeRound().getStart();
        OffsetDateTime end = contest.getResumeRound().getEnd();

        if (timestamp.isBefore(start) || timestamp.isAfter(end)) {
            throw new CustomException("Resume submission window closed", HttpStatus.BAD_REQUEST);
        }
    }

    public void verifyCodingTimeOpen(OffsetDateTime timestamp, Contest contest) {
        OffsetDateTime start = contest.getCodingRound().getStart();
        OffsetDateTime end = contest.getCodingRound().getEnd();

        if (timestamp.isBefore(start) || timestamp.isAfter(end)) {
            throw new CustomException("Coding submission window closed", HttpStatus.BAD_REQUEST);
        }
    }

    public void verifyConceptTimeOpen(OffsetDateTime timestamp, Contest contest) {
        OffsetDateTime start = contest.getConceptRound().getStart();
        OffsetDateTime end = contest.getConceptRound().getEnd();

        if (timestamp.isBefore(start) || timestamp.isAfter(end)) {
            throw new CustomException("Concept submission window closed", HttpStatus.BAD_REQUEST);
        }
    }

    public void verifyHrTimeOpen(OffsetDateTime timestamp, Contest contest) {
        OffsetDateTime start = contest.getHrRound().getStart();
        OffsetDateTime end = contest.getHrRound().getEnd();

        if (timestamp.isBefore(start) || timestamp.isAfter(end)) {
            throw new CustomException("HR submission window closed", HttpStatus.BAD_REQUEST);
        }
    }

    public void verifyCodingTime(OffsetDateTime timestamp, Contest contest, ContestCandidateResponse contestCandidate) {
        Map<String, Object> coding = contestCandidate.getCoding();
        if (coding == null) {
            throw new CustomException("Coding round not started", HttpStatus.BAD_REQUEST);
        }

        OffsetDateTime startTime = (OffsetDateTime) coding.get("start_time");
        OffsetDateTime endTime = (OffsetDateTime) coding.get("end_time");

        if (timestamp.isBefore(startTime) || timestamp.isAfter(endTime)) {
            throw new CustomException("Coding submission window closed", HttpStatus.BAD_REQUEST);
        }
    }

    public void verifyConceptTime(OffsetDateTime timestamp, Contest contest, ContestCandidateResponse contestCandidate) {
        Map<String, Object> concept = contestCandidate.getConcept();
        if (concept == null) {
            throw new CustomException("Concept round not started", HttpStatus.BAD_REQUEST);
        }

        OffsetDateTime startTime = (OffsetDateTime) concept.get("start_time");
        OffsetDateTime endTime = (OffsetDateTime) concept.get("end_time");

        if (timestamp.isBefore(startTime) || timestamp.isAfter(endTime)) {
            throw new CustomException("Concept submission window closed", HttpStatus.BAD_REQUEST);
        }
    }

    public void verifyHrTime(OffsetDateTime timestamp, Contest contest, ContestCandidateResponse contestCandidate) {
        Map<String, Object> hr = contestCandidate.getHr();
        if (hr == null) {
            throw new CustomException("HR round not started", HttpStatus.BAD_REQUEST);
        }

        OffsetDateTime startTime = (OffsetDateTime) hr.get("start_time");
        OffsetDateTime endTime = (OffsetDateTime) hr.get("end_time");

        if (timestamp.isBefore(startTime) || timestamp.isAfter(endTime)) {
            throw new CustomException("HR submission window closed", HttpStatus.BAD_REQUEST);
        }
    }

    // ==========================================
    // 3. SUBMISSION STATUS VERIFICATIONS
    // ==========================================

    public void verifyCodingSubmit(ContestCandidateResponse contestCandidate) {
        Map<String, Object> coding = contestCandidate.getCoding();
        if (coding == null) {
            throw new CustomException("Coding round not started", HttpStatus.BAD_REQUEST);
        }
        if (coding.get("submitted_at") != null) {
            throw new CustomException("Candidate already submitted", HttpStatus.BAD_REQUEST);
        }
    }

    public void verifyConceptSubmit(ContestCandidateResponse contestCandidate) {
        Map<String, Object> concept = contestCandidate.getConcept();
        if (concept == null) {
            throw new CustomException("Concept round not started", HttpStatus.BAD_REQUEST);
        }
        if (concept.get("submitted_at") != null) {
            throw new CustomException("Candidate already submitted", HttpStatus.BAD_REQUEST);
        }
    }

    public void verifyHrSubmit(ContestCandidateResponse contestCandidate) {
        Map<String, Object> hr = contestCandidate.getHr();
        if (hr == null) {
            throw new CustomException("HR round not started", HttpStatus.BAD_REQUEST);
        }
        if (hr.get("submitted_at") != null) {
            throw new CustomException("Candidate already submitted", HttpStatus.BAD_REQUEST);
        }
    }

    // ==========================================
    // 4. QUESTION VERIFICATIONS
    // ==========================================

    public void verifyCodingQuestion(Contest contest, String questionId) {
        // Validation check against coding question bank
    }

    public void verifyConceptQuestion(Contest contest, String questionId) {
        // Validation check against concept question bank
    }

    public void verifyHrQuestion(Contest contest, String questionId) {
        // Validation check against HR question bank
    }

    // ==========================================
    // 5. RESULT DECLARATION VERIFICATIONS
    // ==========================================

    public void verifyResumeResultTime(OffsetDateTime timestamp, Contest contest) {
        if (timestamp.isBefore(contest.getResumeRound().getResult())) {
            throw new CustomException("Resume results not declared yet", HttpStatus.FORBIDDEN);
        }
    }

    public void verifyCodingResultTime(OffsetDateTime timestamp, Contest contest) {
        if (timestamp.isBefore(contest.getCodingRound().getResult())) {
            throw new CustomException("Coding results not declared yet", HttpStatus.FORBIDDEN);
        }
    }

    public void verifyConceptResultTime(OffsetDateTime timestamp, Contest contest) {
        if (timestamp.isBefore(contest.getConceptRound().getResult())) {
            throw new CustomException("Concept results not declared yet", HttpStatus.FORBIDDEN);
        }
    }

    public void verifyHrResultTime(OffsetDateTime timestamp, Contest contest) {
        if (timestamp.isBefore(contest.getHrRound().getResult())) {
            throw new CustomException("HR results not declared yet", HttpStatus.FORBIDDEN);
        }
    }

    public void verifyLeaderboardDeclareTime(OffsetDateTime timestamp, Contest contest) {
        if (timestamp.isBefore(contest.getLeaderboardDeclareTime())) {
            throw new CustomException("Leaderboard not declared yet", HttpStatus.FORBIDDEN);
        }
    }

    // ==========================================
    // 6. LEADERBOARD ADVANCEMENT VERIFICATIONS
    // ==========================================

    public void verifyCandidatePassedResume(UUID candidateId, UUID contestId) {
        ContestLeaderboard leaderboard = contestLeaderboardRepository.findByContestId(contestId)
                .orElseThrow(() -> new CustomException("Leaderboard not generated", HttpStatus.NOT_FOUND));

        List<UUID> passed = leaderboard.getSelectedResumeCandidates() != null
                ? leaderboard.getSelectedResumeCandidates()
                : List.of();

        if (!passed.contains(candidateId)) {
            throw new CustomException("Candidate did not pass resume round", HttpStatus.FORBIDDEN);
        }
    }

    public void verifyCandidatePassedCoding(UUID candidateId, UUID contestId) {
        ContestLeaderboard leaderboard = contestLeaderboardRepository.findByContestId(contestId)
                .orElseThrow(() -> new CustomException("Leaderboard not generated", HttpStatus.NOT_FOUND));

        List<UUID> passed = leaderboard.getSelectedCodingCandidates() != null
                ? leaderboard.getSelectedCodingCandidates()
                : List.of();

        if (!passed.contains(candidateId)) {
            throw new CustomException("Candidate did not pass coding round", HttpStatus.FORBIDDEN);
        }
    }

    public void verifyCandidatePassedConcept(UUID candidateId, UUID contestId) {
        ContestLeaderboard leaderboard = contestLeaderboardRepository.findByContestId(contestId)
                .orElseThrow(() -> new CustomException("Leaderboard not generated", HttpStatus.NOT_FOUND));

        List<UUID> passed = leaderboard.getSelectedConceptCandidates() != null
                ? leaderboard.getSelectedConceptCandidates()
                : List.of();

        if (!passed.contains(candidateId)) {
            throw new CustomException("Candidate did not pass concept round", HttpStatus.FORBIDDEN);
        }
    }

    // ==========================================
    // 7. ROUND DATA EXTRACTORS
    // ==========================================

    public Map<String, Object> verifyResumeRoundData(ContestCandidateResponse contestCandidate) {
        Map<String, Object> resume = contestCandidate.getResume();
        if (resume == null) {
            throw new CustomException("Resume round data not found", HttpStatus.NOT_FOUND);
        }
        return resume;
    }

    public Map<String, Object> verifyCodingRoundData(ContestCandidateResponse contestCandidate) {
        Map<String, Object> coding = contestCandidate.getCoding();
        if (coding == null) {
            throw new CustomException("Coding round data not found", HttpStatus.NOT_FOUND);
        }
        return coding;
    }

    public Map<String, Object> verifyConceptRoundData(ContestCandidateResponse contestCandidate) {
        Map<String, Object> concept = contestCandidate.getConcept();
        if (concept == null) {
            throw new CustomException("Concept round data not found", HttpStatus.NOT_FOUND);
        }
        return concept;
    }

    public Map<String, Object> verifyHrRoundData(ContestCandidateResponse contestCandidate) {
        Map<String, Object> hr = contestCandidate.getHr();
        if (hr == null) {
            throw new CustomException("HR round data not found", HttpStatus.NOT_FOUND);
        }
        return hr;
    }

    private UUID parseUuid(String idStr, String errorMessage, HttpStatus status) {
        try {
            return UUID.fromString(idStr);
        } catch (Exception e) {
            throw new CustomException(errorMessage, status);
        }
    }
}