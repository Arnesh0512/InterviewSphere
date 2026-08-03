package com.arnesh.interviewsphere.service;

import com.arnesh.interviewsphere.ai.ContestAiService;
import com.arnesh.interviewsphere.dto.response.ContestAiResponses;
import com.arnesh.interviewsphere.entity.*;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.*;
import com.arnesh.interviewsphere.security.UserPrincipal;
import com.arnesh.interviewsphere.util.ContestEvaluationUtil;
import com.arnesh.interviewsphere.util.PdfOcrExtractor;
import com.arnesh.interviewsphere.util.TimeUtil;
import com.arnesh.interviewsphere.verification.CandidateVerificationService;
import com.arnesh.interviewsphere.verification.ContestVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ContestService {

    private final ContestRepository contestRepository;
    private final ContestCandidateResponseRepository contestCandidateResponseRepository;
    private final ContestLeaderboardRepository contestLeaderboardRepository;
    private final LeetCodeQuestionRepository leetCodeQuestionRepository;
    private final CandidateVerificationService candidateVerificationService;
    private final ContestVerificationService contestVerificationService;
    private final ContestAiService contestAiService;
    private final ContestEvaluationUtil contestEvaluationUtil;
    private final PdfOcrExtractor pdfOcrExtractor;
    private final SupabaseStorageService supabaseStorageService;

    // 1. Fetch Available Contests
    public List<Map<String, Object>> fetchAvailableContests(UserPrincipal principal) {
        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        List<String> candidateRoles = candidate.getRoles() != null
                ? candidate.getRoles().stream().map(r -> r.name()).toList()
                : List.of();

        List<String> candidateSkills = candidate.getSkills() != null
                ? candidate.getSkills().stream().map(s -> s.name()).toList()
                : List.of();

        if (candidateRoles.isEmpty() || candidateSkills.isEmpty()) {
            return List.of();
        }

        List<Contest> contests = contestRepository.findAvailableContests(candidateRoles, candidateSkills);

        return contests.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("contest_id", c.getId().toString());
            map.put("company", c.getCompany());
            map.put("role", c.getRole());
            map.put("skills", c.getSkills());
            map.put("contest_start", c.getContestStart());
            map.put("contest_end", c.getContestEnd());
            map.put("last_date_to_register", c.getLastDateToRegister());
            map.put("candidate_capacity", c.getCandidateCapacity());
            map.put("candidate_count", c.getCandidateCount());
            map.put("created_on", c.getCreatedOn());
            return map;
        }).toList();
    }

    // 2. Fetch Contest Details
    public Contest fetchContestDetails(UserPrincipal principal, String contestIdStr) {
        var verified = contestVerificationService.verifyContestId(contestIdStr);
        return verified.getContest();
    }

    // 3. Register for Contest
    @Transactional
    public Map<String, Object> registerForContest(UserPrincipal principal, String contestIdStr) {
        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        var verified = contestVerificationService.verifyContestId(contestIdStr);
        Contest contest = verified.getContest();

        OffsetDateTime timestamp = TimeUtil.generateTimestamp();

        contestVerificationService.verifyCandidateEligibility(timestamp, candidate, contest);
        contestVerificationService.verifyContestRegistry(candidate, contest, "N");

        if (contest.getCandidateCount() >= contest.getCandidateCapacity()) {
            throw new CustomException("Contest full or already registered", HttpStatus.BAD_REQUEST);
        }

        contest.setCandidateCount(contest.getCandidateCount() + 1);
        contestRepository.save(contest);

        ContestCandidateResponse response = ContestCandidateResponse.builder()
                .contestId(contest.getId())
                .candidateId(candidate.getId())
                .createdOn(timestamp)
                .build();

        contestCandidateResponseRepository.save(response);

        return Map.of("success", true, "message", "Registered successfully");
    }

    // 4. Unregister from Contest
    @Transactional
    public Map<String, Object> unregisterFromContest(UserPrincipal principal, String contestIdStr) {
        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        var verified = contestVerificationService.verifyContestId(contestIdStr);
        Contest contest = verified.getContest();

        contestVerificationService.verifyContestRegistry(candidate, contest, "Y");
        contestVerificationService.verifyUnregisterTime(TimeUtil.generateTimestamp(), contest);

        contest.setCandidateCount(Math.max(0, contest.getCandidateCount() - 1));
        contestRepository.save(contest);

        contestCandidateResponseRepository.deleteByContestIdAndCandidateId(contest.getId(), candidate.getId());

        return Map.of("success", true, "message", "Unregistered successfully");
    }

    // 5. Submit Resume Round
    @Transactional
    public Map<String, Object> submitResume(UserPrincipal principal, String contestIdStr, OffsetDateTime frontendTimestamp, MultipartFile file, String ocrMode) {
        OffsetDateTime backendTimestamp = TimeUtil.generateTimestamp();

        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        var verified = contestVerificationService.verifyContestId(contestIdStr);
        Contest contest = verified.getContest();

        ContestCandidateResponse candidateResponse = contestVerificationService.verifyContestRegistry(candidate, contest, "Y");
        OffsetDateTime timestamp = contestVerificationService.verifyTimestamp(frontendTimestamp, backendTimestamp);
        contestVerificationService.verifyResumeTimeOpen(timestamp, contest);

        if (candidateResponse.getResume() != null && !candidateResponse.getResume().isEmpty()) {
            throw new CustomException("Resume already submitted", HttpStatus.BAD_REQUEST);
        }

        if (file.getContentType() == null || !file.getContentType().equalsIgnoreCase("application/pdf")) {
            throw new CustomException("Only PDF allowed", HttpStatus.BAD_REQUEST);
        }

        // Upload directly to Supabase and retrieve public URL
        String fileUrl = supabaseStorageService.uploadFile("contest-resumes", file);

        File tempFile;
        try {
            tempFile = File.createTempFile("contest_resume_", ".pdf");
            file.transferTo(tempFile);
            tempFile.deleteOnExit();
        } catch (IOException e) {
            throw new CustomException("Failed to save temporary PDF", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String resumeText;
        try {
            if ("Y".equalsIgnoreCase(ocrMode)) {
                resumeText = pdfOcrExtractor.extractTextWithOcr(tempFile);
            } else {
                resumeText = pdfOcrExtractor.extractTextWithoutOcr(tempFile);
            }
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }

        if (resumeText == null || resumeText.isBlank()) {
            throw new CustomException("No text extracted from PDF", HttpStatus.BAD_REQUEST);
        }

        List<String> skills = contest.getSkills().stream().map(s -> s.name()).toList();
        
        // Correctly extract the list of question objects from the resume round
        Object resumeQuestionsObj = contest.getResumeRound().getQuestions();
        List<Map<String, Object>> formattedResumeQuestions = new ArrayList<>();
        
        if (resumeQuestionsObj instanceof List<?> rawList) {
            for (Object item : rawList) {
                if (item instanceof Map<?, ?> qMap) {
                    Map<String, Object> formattedMap = new HashMap<>();
                    formattedMap.put("question_id", qMap.get("question_id"));
                    formattedMap.put("question", qMap.get("question"));
                    formattedResumeQuestions.add(formattedMap);
                }
            }
        }

        ContestAiResponses.ContestEvaluationResponse evalResponse = contestAiService.evaluateResumeScore(
                resumeText, formattedResumeQuestions, contest.getCompany().name(), contest.getRole().name(), skills);

        String summary = contestAiService.generateSummary(resumeText);

        Map<String, Object> resumeMap = new HashMap<>();
        resumeMap.put("summary", summary);
        resumeMap.put("file_url", fileUrl); // Storing the Supabase URL
        resumeMap.put("question_bank", evalResponse.getResults());
        resumeMap.put("overall_feedback", evalResponse.getOverallFeedback());
        resumeMap.put("submitted_at", timestamp);

        candidateResponse.setResume(resumeMap);
        contestCandidateResponseRepository.save(candidateResponse);

        return Map.of("success", true, "file_url", fileUrl);
    }

    // 6. Get Resume Round Leaderboard
    public Map<String, Object> getResumeLeaderboard(UserPrincipal principal, String contestIdStr) {
        var verified = contestVerificationService.verifyContestId(contestIdStr);
        Contest contest = verified.getContest();

        contestVerificationService.verifyResumeResultTime(TimeUtil.generateTimestamp(), contest);

        ContestLeaderboard leaderboardDoc = contestLeaderboardRepository.findByContestId(contest.getId())
                .orElseThrow(() -> new CustomException("Leaderboard not generated", HttpStatus.NOT_FOUND));

        boolean selected = leaderboardDoc.getSelectedResumeCandidates() != null &&
                leaderboardDoc.getSelectedResumeCandidates().contains(principal.getId());

        return Map.of(
                "success", true,
                "data", leaderboardDoc.getLeaderboard(),
                "selection", selected
        );
    }

    // 7. Submit Coding Round
    @Transactional
    public Map<String, Object> submitCoding(UserPrincipal principal, String contestIdStr, OffsetDateTime frontendTimestamp) {
        OffsetDateTime backendTimestamp = TimeUtil.generateTimestamp();
        OffsetDateTime timestamp = contestVerificationService.verifyTimestamp(frontendTimestamp, backendTimestamp);

        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        var verified = contestVerificationService.verifyContestId(contestIdStr);
        Contest contest = verified.getContest();

        ContestCandidateResponse candidateResponse = contestVerificationService.verifyContestRegistry(candidate, contest, "Y");
        contestVerificationService.verifyCandidatePassedResume(candidate.getId(), contest.getId());
        contestVerificationService.verifyCodingTime(timestamp, contest, candidateResponse);
        contestVerificationService.verifyCodingSubmit(candidateResponse);

        if (candidateResponse.getCoding() == null) {
            candidateResponse.setCoding(new HashMap<>());
        }
        candidateResponse.getCoding().put("submitted_at", timestamp);

        contestEvaluationUtil.generateCodingScores(contest.getId(), candidate.getId(), candidateResponse);

        return Map.of("success", true, "message", "Coding submitted successfully");
    }

    // 8. Get Coding Round Questions
    @Transactional
    public Map<String, Object> getCodingQuestions(UserPrincipal principal, String contestIdStr) {
        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        var verified = contestVerificationService.verifyContestId(contestIdStr);
        Contest contest = verified.getContest();

        ContestCandidateResponse candidateResponse = contestVerificationService.verifyContestRegistry(candidate, contest, "Y");
        contestVerificationService.verifyCandidatePassedResume(candidate.getId(), contest.getId());
        contestVerificationService.verifyCodingTimeOpen(TimeUtil.generateTimestamp(), contest);

        Map<String, Object> codingMap = candidateResponse.getCoding() != null ? candidateResponse.getCoding() : new HashMap<>();

        OffsetDateTime startTime;
        OffsetDateTime endTime;

        if (codingMap.containsKey("start_time")) {
            startTime = (OffsetDateTime) codingMap.get("start_time");
            endTime = (OffsetDateTime) codingMap.get("end_time");
        } else {
            startTime = TimeUtil.generateTimestamp();
            int duration = contest.getCodingRound().getDuration();
            endTime = startTime.plusMinutes(duration + 1);

            List<Map<String, Object>> questionBank = new ArrayList<>();
            List<LeetCodeQuestion> questions = leetCodeQuestionRepository.findRandomByCompanyAndDifficulty(
                    contest.getCompany().name(), "Medium", contest.getCodingQuestionsCount());

            for (LeetCodeQuestion q : questions) {
                Map<String, Object> qMap = new HashMap<>();
                qMap.put("questionId", q.getQuestionId());
                qMap.put("language", null);
                qMap.put("answer", null);
                qMap.put("timestamp", null);
                qMap.put("feedback", null);
                qMap.put("score", null);
                questionBank.add(qMap);
            }

            codingMap.put("start_time", startTime);
            codingMap.put("end_time", endTime);
            codingMap.put("questionBank", questionBank);
            codingMap.put("submitted_at", null);

            candidateResponse.setCoding(codingMap);
            contestCandidateResponseRepository.save(candidateResponse);
        }

        return Map.of(
                "success", true,
                "duration", contest.getCodingRound().getDuration(),
                "start_time", startTime,
                "end_time", endTime
        );
    }

    // 9. Submit Coding Round Single Answer
    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> submitCodingAnswer(UserPrincipal principal, String contestIdStr, Integer questionId, String answer, String language, OffsetDateTime frontendTimestamp) {
        OffsetDateTime backendTimestamp = TimeUtil.generateTimestamp();
        OffsetDateTime timestamp = contestVerificationService.verifyTimestamp(frontendTimestamp, backendTimestamp);

        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        var verified = contestVerificationService.verifyContestId(contestIdStr);
        Contest contest = verified.getContest();

        ContestCandidateResponse candidateResponse = contestVerificationService.verifyContestRegistry(candidate, contest, "Y");
        contestVerificationService.verifyCandidatePassedResume(candidate.getId(), contest.getId());
        contestVerificationService.verifyCodingQuestion(contest, questionId);
        contestVerificationService.verifyCodingTime(timestamp, contest, candidateResponse);
        contestVerificationService.verifyCodingSubmit(candidateResponse);

        Map<String, Object> codingData = candidateResponse.getCoding();
        if (codingData != null && codingData.containsKey("questionBank")) {
            List<Map<String, Object>> questionBank = (List<Map<String, Object>>) codingData.get("questionBank");
            for (Map<String, Object> q : questionBank) {
                if (Objects.equals(q.get("questionId"), questionId)) {
                    q.put("language", language);
                    q.put("answer", answer);
                    q.put("timestamp", timestamp);
                    break;
                }
            }
            contestCandidateResponseRepository.save(candidateResponse);
        }

        return Map.of("success", true, "message", "Answer saved");
    }

    // 10. Get Coding Leaderboard
    public Map<String, Object> getCodingLeaderboard(UserPrincipal principal, String contestIdStr) {
        var verified = contestVerificationService.verifyContestId(contestIdStr);
        Contest contest = verified.getContest();

        contestVerificationService.verifyCodingResultTime(TimeUtil.generateTimestamp(), contest);

        ContestLeaderboard leaderboardDoc = contestLeaderboardRepository.findByContestId(contest.getId())
                .orElseThrow(() -> new CustomException("Leaderboard not generated", HttpStatus.NOT_FOUND));

        boolean selected = leaderboardDoc.getSelectedCodingCandidates() != null &&
                leaderboardDoc.getSelectedCodingCandidates().contains(principal.getId());

        return Map.of(
                "success", true,
                "data", leaderboardDoc.getLeaderboard(),
                "selection", selected
        );
    }

    // 11. Submit Concept Round
    @Transactional
    public Map<String, Object> submitConcept(UserPrincipal principal, String contestIdStr, OffsetDateTime frontendTimestamp) {
        OffsetDateTime backendTimestamp = TimeUtil.generateTimestamp();
        OffsetDateTime timestamp = contestVerificationService.verifyTimestamp(frontendTimestamp, backendTimestamp);

        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        var verified = contestVerificationService.verifyContestId(contestIdStr);
        Contest contest = verified.getContest();

        ContestCandidateResponse candidateResponse = contestVerificationService.verifyContestRegistry(candidate, contest, "Y");
        contestVerificationService.verifyCandidatePassedCoding(candidate.getId(), contest.getId());
        contestVerificationService.verifyConceptTime(timestamp, contest, candidateResponse);
        contestVerificationService.verifyConceptSubmit(candidateResponse);

        if (candidateResponse.getConcept() == null) {
            candidateResponse.setConcept(new HashMap<>());
        }
        candidateResponse.getConcept().put("submitted_at", timestamp);

        contestEvaluationUtil.generateConceptScores(contest.getId(), candidate.getId(), candidateResponse);

        return Map.of("success", true, "message", "Concept submitted successfully");
    }

    // 12. Submit HR Round
    @Transactional
    public Map<String, Object> submitHr(UserPrincipal principal, String contestIdStr, OffsetDateTime frontendTimestamp) {
        OffsetDateTime backendTimestamp = TimeUtil.generateTimestamp();
        OffsetDateTime timestamp = contestVerificationService.verifyTimestamp(frontendTimestamp, backendTimestamp);

        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        var verified = contestVerificationService.verifyContestId(contestIdStr);
        Contest contest = verified.getContest();

        ContestCandidateResponse candidateResponse = contestVerificationService.verifyContestRegistry(candidate, contest, "Y");
        contestVerificationService.verifyCandidatePassedConcept(candidate.getId(), contest.getId());
        contestVerificationService.verifyHrTime(timestamp, contest, candidateResponse);
        contestVerificationService.verifyHrSubmit(candidateResponse);

        if (candidateResponse.getHr() == null) {
            candidateResponse.setHr(new HashMap<>());
        }
        candidateResponse.getHr().put("submitted_at", timestamp);

        contestEvaluationUtil.generateHrScores(contest.getId(), candidate.getId(), candidateResponse);

        return Map.of("success", true, "message", "HR submitted successfully");
    }

    // 13. Get Final Leaderboard
    public Map<String, Object> getFinalLeaderboard(UserPrincipal principal, String contestIdStr) {
        var verified = contestVerificationService.verifyContestId(contestIdStr);
        Contest contest = verified.getContest();

        contestVerificationService.verifyLeaderboardDeclareTime(TimeUtil.generateTimestamp(), contest);

        ContestLeaderboard leaderboardDoc = contestLeaderboardRepository.findByContestId(contest.getId())
                .orElseThrow(() -> new CustomException("Leaderboard not generated", HttpStatus.NOT_FOUND));

        return Map.of(
                "success", true,
                "data", leaderboardDoc.getLeaderboard()
        );
    }

    // 14. Get Resume Question Bank
    public Map<String, Object> getResumeQuestionBank(UserPrincipal principal, String contestIdStr) {
        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        var verified = contestVerificationService.verifyContestId(contestIdStr);
        ContestCandidateResponse candidateResponse = contestVerificationService.verifyContestRegistry(candidate, verified.getContest(), "Y");

        var resume = contestVerificationService.verifyResumeRoundData(candidateResponse);
        return Map.of("success", true, "data", resume);
    }

    // 15. Get Coding Question Bank
    public Map<String, Object> getCodingQuestionBank(UserPrincipal principal, String contestIdStr) {
        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        var verified = contestVerificationService.verifyContestId(contestIdStr);
        ContestCandidateResponse candidateResponse = contestVerificationService.verifyContestRegistry(candidate, verified.getContest(), "Y");

        var coding = contestVerificationService.verifyCodingRoundData(candidateResponse);
        return Map.of("success", true, "data", coding);
    }

    // 16. Get Concept Question Bank
    public Map<String, Object> getConceptQuestionBank(UserPrincipal principal, String contestIdStr) {
        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        var verified = contestVerificationService.verifyContestId(contestIdStr);
        ContestCandidateResponse candidateResponse = contestVerificationService.verifyContestRegistry(candidate, verified.getContest(), "Y");

        var concept = contestVerificationService.verifyConceptRoundData(candidateResponse);
        return Map.of("success", true, "data", concept);
    }

    // 17. Get HR Question Bank
    public Map<String, Object> getHrQuestionBank(UserPrincipal principal, String contestIdStr) {
        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        var verified = contestVerificationService.verifyContestId(contestIdStr);
        ContestCandidateResponse candidateResponse = contestVerificationService.verifyContestRegistry(candidate, verified.getContest(), "Y");

        var hr = contestVerificationService.verifyHrRoundData(candidateResponse);
        return Map.of("success", true, "data", hr);
    }
}