package com.arnesh.interviewsphere.service;

import com.arnesh.interviewsphere.ai.AdminAiService;
import com.arnesh.interviewsphere.dto.request.ContestCreateRequest;
import com.arnesh.interviewsphere.dto.request.UserCreateRequest;
import com.arnesh.interviewsphere.dto.response.LeaderboardEntryResponse;
import com.arnesh.interviewsphere.entity.*;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.*;
import com.arnesh.interviewsphere.security.UserPrincipal;
import com.arnesh.interviewsphere.util.AdminUtil;
import com.arnesh.interviewsphere.util.ContestUtil;
import com.arnesh.interviewsphere.util.TimeUtil;
import com.arnesh.interviewsphere.verification.AdminVerificationService;
import com.arnesh.interviewsphere.verification.CandidateVerificationService;
import com.arnesh.interviewsphere.verification.ContestValidationService;
import com.arnesh.interviewsphere.verification.ContestVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final ContestRepository contestRepository;
    private final ContestCandidateResponseRepository contestCandidateResponseRepository;
    private final ContestLeaderboardRepository contestLeaderboardRepository;
    private final AdminVerificationService adminVerificationService;
    private final ContestValidationService contestValidationService;
    private final ContestVerificationService contestVerificationService;
    private final CandidateVerificationService candidateVerificationService;
    private final AdminAiService adminAiService;
    private final AdminUtil adminUtil;
    private final NormalizerService normalizerService;
    private final ContestUtil contestUtil;

    // 1. Register Admin Profile
    @Transactional
    public Map<String, Object> registerAdmin(UserPrincipal principal, UserCreateRequest request) {
        var verified = adminVerificationService.verifyAdminById(principal.getId().toString(), "Y");
        Admin admin = verified.getAdmin();

        if (!admin.getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new CustomException("Email mismatch", HttpStatus.BAD_REQUEST);
        }

        admin.setFullName(request.getFullName());
        adminRepository.save(admin);

        return Map.of("success", true, "message", "admin registered successfully");
    }

    // 2. Change Admin Details
    @Transactional
    public Map<String, Object> changeAdminDetails(UserPrincipal principal, UserCreateRequest request) {
        return registerAdmin(principal, request);
    }

    // 3. Get Admin Profile
    public Map<String, Object> getAdminProfile(UserPrincipal principal) {
        var verified = adminVerificationService.verifyAdminById(principal.getId().toString(), "Y");
        Admin admin = verified.getAdmin();

        Map<String, Object> data = new HashMap<>();
        data.put("admin_id", admin.getId().toString());
        data.put("email", admin.getEmail());
        data.put("full_name", admin.getFullName());
        data.put("created_on", admin.getCreatedOn());

        return Map.of("success", true, "data", data);
    }

    // 4. Create Contest
    @Transactional
    public Map<String, Object> createContest(UserPrincipal principal, ContestCreateRequest request) {
        var verifiedAdmin = adminVerificationService.verifyAdminById(principal.getId().toString(), "Y");

        contestValidationService.verifyDuplicateContest(request);
        contestValidationService.validateContestData(request);

        List<String> skillStrings = request.getSkills().stream().map(s -> s.name()).toList();
        adminAiService.validateRoleSkills(request.getRole().name(), skillStrings);

        List<String> resumeQuestions = adminAiService.generateResumeQuestions(
                request.getCompany().name(), request.getRole().name(), skillStrings, request.getResumeQuestionsCount());

        List<String> codingQuestionIds = adminUtil.generateCodingIds(
                request.getCompany().name(), request.getCodingQuestionsCount());

        List<String> conceptQuestions = adminAiService.generateConceptQuestions(
                request.getRole().name(), skillStrings, request.getConceptQuestionsCount());

        List<String> hrQuestions = adminAiService.generateHrQuestions(
                request.getRole().name(), Math.max(1, request.getHrQuestionsCount() - 1));

        Map<String, String> resumeQMap = new HashMap<>();
        for (int i = 0; i < resumeQuestions.size(); i++) {
            resumeQMap.put(String.valueOf(i + 1), resumeQuestions.get(i));
        }

        Map<String, String> conceptQMap = new HashMap<>();
        for (int i = 0; i < conceptQuestions.size(); i++) {
            conceptQMap.put(String.valueOf(i + 1), conceptQuestions.get(i));
        }

        Map<String, String> hrQMap = new HashMap<>();
        hrQMap.put("1", "Introduce Yourself");
        for (int i = 0; i < hrQuestions.size(); i++) {
            hrQMap.put(String.valueOf(i + 2), hrQuestions.get(i));
        }

        Contest.RoundTimeDto resumeRound = Contest.RoundTimeDto.builder()
                .start(request.getResumeRound().getStart())
                .end(request.getResumeRound().getEnd())
                .duration(request.getResumeRound().getDuration())
                .result(request.getResumeRound().getResult())
                .build();

        Contest.RoundTimeDto codingRound = Contest.RoundTimeDto.builder()
                .start(request.getCodingRound().getStart())
                .end(request.getCodingRound().getEnd())
                .duration(request.getCodingRound().getDuration())
                .result(request.getCodingRound().getResult())
                .questions(codingQuestionIds)
                .build();

        Contest.RoundTimeDto conceptRound = Contest.RoundTimeDto.builder()
                .start(request.getConceptRound().getStart())
                .end(request.getConceptRound().getEnd())
                .duration(request.getConceptRound().getDuration())
                .result(request.getConceptRound().getResult())
                .build();

        Contest.RoundTimeDto hrRound = Contest.RoundTimeDto.builder()
                .start(request.getHrRound().getStart())
                .end(request.getHrRound().getEnd())
                .duration(request.getHrRound().getDuration())
                .result(request.getHrRound().getResult())
                .build();

        Contest contest = Contest.builder()
                .adminCreated(verifiedAdmin.getAdmin())
                .company(request.getCompany())
                .role(request.getRole())
                .skills(request.getSkills())
                .lastDateToRegister(request.getLastDateToRegister())
                .contestStart(request.getContestStart())
                .contestEnd(request.getContestEnd())
                .leaderboardDeclareTime(request.getLeaderboardDeclareTime())
                .resumeRound(resumeRound)
                .codingRound(codingRound)
                .conceptRound(conceptRound)
                .hrRound(hrRound)
                .resumeQuestionsCount(request.getResumeQuestionsCount())
                .codingQuestionsCount(request.getCodingQuestionsCount())
                .conceptQuestionsCount(request.getConceptQuestionsCount())
                .hrQuestionsCount(request.getHrQuestionsCount())
                .candidateCapacity(request.getCandidateCapacity())
                .selectedResume(request.getSelectedResume())
                .selectedCoding(request.getSelectedCoding())
                .selectedConcept(request.getSelectedConcept())
                .selectedHr(request.getSelectedHr())
                .candidateCount(0)
                .build();

        contestRepository.save(contest);

        return Map.of("success", true);
    }

    // 5. Get All Contest Summaries
    public List<Map<String, Object>> getAllContestIds(UserPrincipal principal) {
        adminVerificationService.verifyAdminById(principal.getId().toString(), "Y");

        List<Contest> contests = contestRepository.findAll();
        return contests.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("contest_id", c.getId().toString());
            map.put("company", c.getCompany());
            map.put("role", c.getRole());
            map.put("skills", c.getSkills());
            map.put("candidate_capacity", c.getCandidateCapacity());
            map.put("contest_start", c.getContestStart());
            map.put("created_on", c.getCreatedOn());
            return map;
        }).toList();
    }

    // 6. Get Single Contest Details
    public Contest getContestDetails(UserPrincipal principal, String contestIdStr) {
        adminVerificationService.verifyAdminById(principal.getId().toString(), "Y");
        return contestValidationService.verifyContestId(contestIdStr);
    }

    // 7. Generate Resume Results
    @Transactional
    @SuppressWarnings("unchecked")
    public void generateResumeResult(UserPrincipal principal, String contestIdStr) {
        adminVerificationService.verifyAdminById(principal.getId().toString(), "Y");
        Contest contest = contestValidationService.verifyContestId(contestIdStr);

        List<ContestCandidateResponse> responses = contestCandidateResponseRepository.findByContestId(contest.getId());
        if (responses.isEmpty()) {
            throw new CustomException("No resumes submitted for this contest", HttpStatus.NOT_FOUND);
        }

        int qCount = contest.getResumeQuestionsCount();
        List<List<NormalizerService.CandidateQuestionScore>> questionScores = new ArrayList<>();
        for (int i = 0; i < qCount; i++) questionScores.add(new ArrayList<>());

        for (ContestCandidateResponse response : responses) {
            Map<String, Object> resume = response.getResume();
            if (resume != null && resume.containsKey("question_bank")) {
                List<Map<String, Object>> qb = (List<Map<String, Object>>) resume.get("question_bank");
                for (Map<String, Object> q : qb) {
                    int idx = Integer.parseInt(q.get("question_id").toString()) - 1;
                    double score = Double.parseDouble(q.getOrDefault("score", 0.0).toString());
                    OffsetDateTime time = (OffsetDateTime) resume.get("submitted_at");

                    if (idx >= 0 && idx < qCount) {
                        questionScores.get(idx).add(new NormalizerService.CandidateQuestionScore(
                                response.getCandidateId().toString(), score, time));
                    }
                }
            }
        }

        List<NormalizerService.RankedCandidate> ranked = normalizerService.normalizeAndRank(questionScores);

        ContestLeaderboard leaderboard = contestLeaderboardRepository.findByContestId(contest.getId())
                .orElseGet(() -> ContestLeaderboard.builder().contestId(contest.getId()).build());

        List<UUID> selected = ranked.stream()
                .limit(contest.getSelectedResume())
                .map(r -> UUID.fromString(r.candidateId()))
                .toList();

        leaderboard.setSelectedResumeCandidates(selected);
        contestLeaderboardRepository.save(leaderboard);
    }

    // 8. Generate Final Leaderboard
    @Transactional
    public void generateLeaderboard(UserPrincipal principal, String contestIdStr) {
        adminVerificationService.verifyAdminById(principal.getId().toString(), "Y");
        Contest contest = contestValidationService.verifyContestId(contestIdStr);

        ContestLeaderboard leaderboardDoc = contestLeaderboardRepository.findByContestId(contest.getId())
                .orElseThrow(() -> new CustomException("No round leaderboards found", HttpStatus.NOT_FOUND));

        List<List<NormalizerService.RankedCandidate>> sectionOutputs = new ArrayList<>();

        List<NormalizerService.RankedCandidate> finalRanked = normalizerService.finalizeLeaderboard(sectionOutputs);

        List<Map<String, Object>> finalLeaderboard = finalRanked.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("candidate_id", r.candidateId());
            m.put("final_normalized_score", r.finalNormalizedScore());
            m.put("latest_submission", r.latestSubmission());
            m.put("rank", r.rank());
            m.put("percentile", r.percentile());
            return m;
        }).toList();

        leaderboardDoc.setLeaderboard(finalLeaderboard);
        contestLeaderboardRepository.save(leaderboardDoc);
    }

    // 9. View Registered Candidates
    public List<Map<String, Object>> getRegisteredCandidates(UserPrincipal principal, String contestIdStr) {
        adminVerificationService.verifyAdminById(principal.getId().toString(), "Y");
        Contest contest = contestValidationService.verifyContestId(contestIdStr);

        List<ContestCandidateResponse> responses = contestCandidateResponseRepository.findByContestId(contest.getId());

        return responses.stream().map(cc -> {
            var candidateRes = candidateVerificationService.verifyCandidateById(cc.getCandidateId().toString(), "Y");
            Candidate candidate = candidateRes.getCandidate();

            String lastRound = null;
            if (cc.getHr() != null) lastRound = "hr";
            else if (cc.getConcept() != null) lastRound = "concept";
            else if (cc.getCoding() != null) lastRound = "coding";
            else if (cc.getResume() != null) lastRound = "resume";

            Map<String, Object> map = new HashMap<>();
            map.put("candidate_id", cc.getCandidateId().toString());
            map.put("name", candidate.getFullName());
            map.put("email", candidate.getEmail());
            map.put("last_round_participated", lastRound);
            return map;
        }).toList();
    }

    // 10. View Round Leaderboards
    public List<LeaderboardEntryResponse> viewRoundLeaderboard(UserPrincipal principal, String contestIdStr, String roundType) {
        adminVerificationService.verifyAdminById(principal.getId().toString(), "Y");
        Contest contest = contestValidationService.verifyContestId(contestIdStr);

        OffsetDateTime now = TimeUtil.generateTimestamp();

        if ("resume".equalsIgnoreCase(roundType)) contestVerificationService.verifyResumeResultTime(now, contest);
        else if ("coding".equalsIgnoreCase(roundType)) contestVerificationService.verifyCodingResultTime(now, contest);
        else if ("concept".equalsIgnoreCase(roundType)) contestVerificationService.verifyConceptResultTime(now, contest);
        else if ("hr".equalsIgnoreCase(roundType)) contestVerificationService.verifyHrResultTime(now, contest);
        else if ("final".equalsIgnoreCase(roundType)) contestVerificationService.verifyLeaderboardDeclareTime(now, contest);

        ContestLeaderboard leaderboardDoc = contestLeaderboardRepository.findByContestId(contest.getId())
                .orElseThrow(() -> new CustomException("Leaderboard not found", HttpStatus.NOT_FOUND));

        if (leaderboardDoc.getLeaderboard() == null || leaderboardDoc.getLeaderboard().isEmpty()) {
            List<ContestCandidateResponse> responses = contestCandidateResponseRepository.findByContestId(contest.getId());
            return contestUtil.formatLeaderboard(responses);
        }

        List<ContestCandidateResponse> responses = contestCandidateResponseRepository.findByContestId(contest.getId());
        return contestUtil.formatLeaderboard(responses);
    }

    // 11. Get Candidate Contest Details
    public ContestCandidateResponse getCandidateContestDetails(UserPrincipal principal, String contestIdStr, String candidateIdStr) {
        adminVerificationService.verifyAdminById(principal.getId().toString(), "Y");
        Contest contest = contestValidationService.verifyContestId(contestIdStr);

        var candidateRes = candidateVerificationService.verifyCandidateById(candidateIdStr, "Y");
        Candidate candidate = candidateRes.getCandidate();

        return contestCandidateResponseRepository.findByContestIdAndCandidateId(contest.getId(), candidate.getId())
                .orElseThrow(() -> new CustomException("Candidate contest record not found", HttpStatus.NOT_FOUND));
    }

    // 12. Delete Contest
    @Transactional
    public Map<String, Object> deleteContest(UserPrincipal principal, String contestIdStr) {
        adminVerificationService.verifyAdminById(principal.getId().toString(), "Y");
        Contest contest = contestValidationService.verifyContestId(contestIdStr);

        contestCandidateResponseRepository.deleteByContestId(contest.getId());
        contestLeaderboardRepository.deleteByContestId(contest.getId());
        contestRepository.deleteById(contest.getId());

        return Map.of("success", true, "message", "Contest deleted successfully");
    }
}