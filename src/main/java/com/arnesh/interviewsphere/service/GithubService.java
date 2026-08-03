package com.arnesh.interviewsphere.service;

import com.arnesh.interviewsphere.ai.GithubAiService;
import com.arnesh.interviewsphere.dto.request.SaveAnswerRequest;
import com.arnesh.interviewsphere.dto.request.SubmitSessionRequest;
import com.arnesh.interviewsphere.dto.response.GithubAiResponses;
import com.arnesh.interviewsphere.dto.response.GithubRepositoryResponse;
import com.arnesh.interviewsphere.dto.response.PreviousGithubSessionResponse;
import com.arnesh.interviewsphere.dto.response.QuestionSessionResponse;
import com.arnesh.interviewsphere.entity.Candidate;
import com.arnesh.interviewsphere.entity.PracticeGithubTrack;
import com.arnesh.interviewsphere.entity.QuestionSession;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.CandidateRepository;
import com.arnesh.interviewsphere.repository.PracticeGithubTrackRepository;
import com.arnesh.interviewsphere.repository.QuestionSessionRepository;
import com.arnesh.interviewsphere.security.UserPrincipal;
import com.arnesh.interviewsphere.util.GithubUtil;
import com.arnesh.interviewsphere.util.TimeUtil;
import com.arnesh.interviewsphere.verification.CandidateVerificationService;
import com.arnesh.interviewsphere.verification.GithubVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GithubService {

    private final PracticeGithubTrackRepository practiceGithubTrackRepository;
    private final QuestionSessionRepository questionSessionRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateVerificationService candidateVerificationService;
    private final GithubVerificationService githubVerificationService;
    private final GithubAiService githubAiService;
    private final GithubUtil githubUtil;

    // 1. Fetch Repositories
    public List<GithubRepositoryResponse> getRepositories(UserPrincipal principal, String githubLink) {
        githubVerificationService.verifyGithubLink(githubLink);
        return githubUtil.fetchRepositories(githubLink);
    }

    // 2. Fetch Repo Details & Save Track
    @Transactional
    public Map<String, Object> getRepositoryDetails(UserPrincipal principal, String githubLink, String repoLink) {
        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        githubVerificationService.verifyGithubLinkRepo(githubLink, repoLink);
        GithubAiResponses.ProcessRepoResponse repoDetails = githubAiService.processRepo(repoLink);

        int nextGithubNum = (candidate.getTotalGithubs() != null ? candidate.getTotalGithubs() : 0) + 1;

        PracticeGithubTrack track = PracticeGithubTrack.builder()
                .candidateId(candidate.getId())
                .githubLink(githubLink)
                .selectedRepo(repoDetails.getRepoName())
                .repoSummary(repoDetails.getSummary())
                .totalSessions(0)
                .build();

        practiceGithubTrackRepository.save(track);

        candidate.setTotalGithubs(nextGithubNum);
        candidateRepository.save(candidate);

        return Map.of("success", true);
    }

    // 3. Get All GitHub Tracks
    public List<Map<String, Object>> getAllGithubs(UserPrincipal principal) {
        List<PracticeGithubTrack> tracks = practiceGithubTrackRepository
                .findByCandidateIdOrderByCreatedOnDesc(principal.getId());

        return tracks.stream().map(track -> {
            Map<String, Object> map = new HashMap<>();
            map.put("github_id", track.getId().toString());
            map.put("github_link", track.getGithubLink());
            map.put("repo_name", track.getSelectedRepo());
            map.put("created_on", track.getCreatedOn());
            return map;
        }).toList();
    }

    // 4. Submit Question Session
    @Transactional
    public void submitSession(UserPrincipal principal, String githubIdStr, String sessionIdStr,
            SubmitSessionRequest request) {
        var verifiedGithub = githubVerificationService.verifyGithub(githubIdStr, principal.getId());
        var verifiedSession = githubVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedGithub.getGithubId());
        QuestionSession session = verifiedSession.getSessionDoc();

        githubVerificationService.verifySessionStatus(session);
        githubVerificationService.verifySessionTime(session, session.getId());

        var verifiedTimestamp = githubVerificationService.verifyTimestamp(request.getFrontendTimestamp());

        session.setStatus("passive");
        session.setSubmittedAtFrontend(verifiedTimestamp.getFrontendTime());
        session.setSubmittedAtBackend(verifiedTimestamp.getBackendTime());

        questionSessionRepository.save(session);
    }

    // 5. Generate New Question Session
    @Transactional
    public QuestionSessionResponse generateQuestions(UserPrincipal principal, String githubIdStr, int numQuestions) {
        var verifiedGithub = githubVerificationService.verifyGithub(githubIdStr, principal.getId());
        PracticeGithubTrack track = verifiedGithub.getGithubTrack();

        int sessionNumber = (track.getTotalSessions() != null ? track.getTotalSessions() : 0) + 1;

        Map<String, Map<String, String>> previousSessions = new HashMap<>();
        if (sessionNumber != 1) {
            PreviousGithubSessionResponse prevResponse = githubUtil.previousGithubSessionQuestions(track.getId(), null,
                    null);
            previousSessions = prevResponse.getSessionDict();
        }

        GithubAiResponses.QuestionListResponse aiQuestions = githubAiService.generateGithubQuestion(
                track.getRepoSummary(), numQuestions, previousSessions);

        List<QuestionSession.Question> questionBank = new ArrayList<>();
        int durationMinutes = numQuestions * 10;

        for (int i = 0; i < aiQuestions.getQuestions().size(); i++) {
            questionBank.add(QuestionSession.Question.builder()
                    .questionNumber(i + 1)
                    .question(aiQuestions.getQuestions().get(i))
                    .answer("")
                    .feedback("")
                    .score(0.0)
                    .build());
        }

        OffsetDateTime now = TimeUtil.generateTimestamp();
        QuestionSession session = QuestionSession.builder()
                .sessionNumber(sessionNumber)
                .githubTrackId(track.getId())
                .time(durationMinutes)
                .questionBank(questionBank)
                .status("active")
                .timestamp(now)
                .build();

        questionSessionRepository.save(session);

        track.setTotalSessions(sessionNumber);
        practiceGithubTrackRepository.save(track);

        return QuestionSessionResponse.builder()
                .sessionId(session.getId().toString())
                .sessionNumber(sessionNumber)
                .status("active")
                .time(durationMinutes)
                .timestamp(now)
                .build();
    }

    // 6. Save Single Answer
    @Transactional
    public void saveAnswer(UserPrincipal principal, String githubIdStr, String sessionIdStr,
            SaveAnswerRequest request) {
        var verifiedGithub = githubVerificationService.verifyGithub(githubIdStr, principal.getId());
        var verifiedSession = githubVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedGithub.getGithubId());
        QuestionSession session = verifiedSession.getSessionDoc();

        githubVerificationService.verifySessionStatus(session);
        githubVerificationService.verifySessionTime(session, session.getId());
        githubVerificationService.verifyQuestionNumber(session, request.getQuestionNumber());

        if (session.getQuestionBank() != null) {
            for (QuestionSession.Question q : session.getQuestionBank()) {
                if (Objects.equals(q.getQuestionNumber(), request.getQuestionNumber())) {
                    q.setAnswer(request.getAnswer());
                    break;
                }
            }
        }

        questionSessionRepository.save(session);
    }

    // 7. Reattempt Session
    @Transactional
    public Map<String, String> reattemptSession(UserPrincipal principal, String githubIdStr, String sessionIdStr) {
        var verifiedGithub = githubVerificationService.verifyGithub(githubIdStr, principal.getId());
        var verifiedSession = githubVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedGithub.getGithubId());
        QuestionSession oldSession = verifiedSession.getSessionDoc();

        List<QuestionSession.Question> newQuestionBank = new ArrayList<>();
        if (oldSession.getQuestionBank() != null) {
            for (QuestionSession.Question q : oldSession.getQuestionBank()) {
                newQuestionBank.add(QuestionSession.Question.builder()
                        .questionNumber(q.getQuestionNumber())
                        .question(q.getQuestion())
                        .answer("")
                        .feedback("")
                        .score(0.0)
                        .build());
            }
        }

        QuestionSession newSession = QuestionSession.builder()
                .sessionNumber(oldSession.getSessionNumber())
                .githubTrackId(verifiedGithub.getGithubId())
                .time(oldSession.getTime())
                .questionBank(newQuestionBank)
                .status("active")
                .timestamp(TimeUtil.generateTimestamp())
                .build();

        questionSessionRepository.save(newSession);

        return Map.of("new_question_session_id", newSession.getId().toString());
    }

    // 8. Fake Submit Session
    @Transactional
    public void fakeSubmitSession(UserPrincipal principal, String githubIdStr, String sessionIdStr) {
        var verifiedGithub = githubVerificationService.verifyGithub(githubIdStr, principal.getId());
        var verifiedSession = githubVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedGithub.getGithubId());
        QuestionSession session = verifiedSession.getSessionDoc();

        githubVerificationService.verifySessionStatus(session);

        OffsetDateTime timestamp = session.getTimestamp() != null ? session.getTimestamp()
                : TimeUtil.generateTimestamp();
        int minutes = session.getTime() != null ? session.getTime() : 0;
        OffsetDateTime autoSubmitTime = timestamp.plusMinutes(minutes + 1);

        session.setStatus("passive");
        session.setSubmittedAtFrontend(autoSubmitTime);
        session.setSubmittedAtBackend(autoSubmitTime);

        questionSessionRepository.save(session);
    }

    // 9. Generate AI Feedback
    @Transactional
    public GithubAiResponses.GithubEvaluationResponse generateFeedback(UserPrincipal principal, String githubIdStr,
            String sessionIdStr) {
        var verifiedGithub = githubVerificationService.verifyGithub(githubIdStr, principal.getId());
        var verifiedSession = githubVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedGithub.getGithubId());
        QuestionSession session = verifiedSession.getSessionDoc();

        githubVerificationService.verifySessionStatus2(session);

        GithubAiResponses.GithubEvaluationResponse evaluation = githubAiService.evaluateGithubAnswers(
                verifiedGithub.getGithubTrack().getRepoSummary(), session.getQuestionBank());

        if (evaluation.getFeedbackPerQuestion() != null) {
            Map<Integer, GithubAiResponses.QuestionFeedback> feedbackMap = new HashMap<>();
            for (var item : evaluation.getFeedbackPerQuestion()) {
                feedbackMap.put(item.getQuestionNumber(), item);
            }

            for (QuestionSession.Question q : session.getQuestionBank()) {
                if (feedbackMap.containsKey(q.getQuestionNumber())) {
                    q.setFeedback(feedbackMap.get(q.getQuestionNumber()).getFeedback());
                    q.setScore(feedbackMap.get(q.getQuestionNumber()).getScore());
                }
            }
        }

        session.setOverallFeedback(evaluation.getOverallFeedback());
        session.setOverallScore(evaluation.getOverallScore());
        questionSessionRepository.save(session);

        return evaluation;
    }

    // 10. Get All Sessions
    public Map<String, Object> getAllSessions(UserPrincipal principal, String githubIdStr) {
        var verifiedGithub = githubVerificationService.verifyGithub(githubIdStr, principal.getId());
        List<QuestionSession> sessions = questionSessionRepository
                .findByGithubTrackIdOrderByTimestampAsc(verifiedGithub.getGithubId());

        List<Map<String, Object>> sessionList = sessions.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("question_session_id", s.getId().toString());
            m.put("session_number", s.getSessionNumber());
            m.put("status", s.getStatus());
            m.put("timestamp", s.getTimestamp());
            return m;
        }).toList();

        return Map.of("github_id", verifiedGithub.getGithubId().toString(), "sessions", sessionList);
    }

    // 11. Get Single Session Data
    public Map<String, Object> getSessionData(UserPrincipal principal, String githubIdStr, String sessionIdStr) {
        var verifiedGithub = githubVerificationService.verifyGithub(githubIdStr, principal.getId());
        var verifiedSession = githubVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedGithub.getGithubId());

        return Map.of("github_id", verifiedGithub.getGithubId().toString(), "session", verifiedSession.getSessionDoc());
    }

    // 12. Delete GitHub Track
    @Transactional
    public void deleteGithub(UserPrincipal principal, String githubIdStr) {
        var verifiedGithub = githubVerificationService.verifyGithub(githubIdStr, principal.getId());
        questionSessionRepository.deleteByGithubTrackId(verifiedGithub.getGithubId());
        practiceGithubTrackRepository.deleteById(verifiedGithub.getGithubId());
    }

    // 13. Delete Single Session
    @Transactional
    public void deleteSession(UserPrincipal principal, String githubIdStr, String sessionIdStr) {
        var verifiedGithub = githubVerificationService.verifyGithub(githubIdStr, principal.getId());
        var verifiedSession = githubVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedGithub.getGithubId());

        long sameSessionCount = questionSessionRepository.countByGithubTrackIdAndSessionNumber(
                verifiedGithub.getGithubId(), verifiedSession.getSessionDoc().getSessionNumber());

        if (sameSessionCount <= 1) {
            throw new CustomException("Can't delete complete session entirely, either reattempt or leave.",
                    HttpStatus.BAD_REQUEST);
        }

        questionSessionRepository.deleteById(verifiedSession.getSessionObjId());
    }

    // 14. Delete Answers and Reattempt
    @Transactional
    public void deleteAndReattempt(UserPrincipal principal, String githubIdStr, String sessionIdStr) {
        var verifiedGithub = githubVerificationService.verifyGithub(githubIdStr, principal.getId());
        var verifiedSession = githubVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedGithub.getGithubId());
        QuestionSession session = verifiedSession.getSessionDoc();

        if (session.getQuestionBank() != null) {
            for (QuestionSession.Question q : session.getQuestionBank()) {
                q.setAnswer("");
                q.setFeedback("");
                q.setScore(0.0);
            }
        }

        session.setOverallFeedback("");
        session.setOverallScore(0.0);
        session.setStatus("active");
        session.setTimestamp(TimeUtil.generateTimestamp());
        session.setSubmittedAtFrontend(null);
        session.setSubmittedAtBackend(null);

        questionSessionRepository.save(session);
    }

    // 15. Progress: Combined Feedback Across Different Sessions
    @Transactional
    public Map<String, Object> combinedFeedbackLastXSessions(UserPrincipal principal, String githubIdStr, int x) {
        if (x <= 1) {
            throw new CustomException("Invalid value of x", HttpStatus.BAD_REQUEST);
        }

        var verifiedGithub = githubVerificationService.verifyGithub(githubIdStr, principal.getId());
        PracticeGithubTrack track = verifiedGithub.getGithubTrack();

        PreviousGithubSessionResponse sessionData = githubUtil.previousGithubSessionQuestions(track.getId(), x, null);
        GithubAiResponses.CombinedFeedbackDetails combinedDetails = githubAiService
                .generateGithubCombinedDiffSessionFeedback(
                        track.getRepoSummary(),new HashMap<>(sessionData.getSessionDict()));

        Map<String, Object> feedbackRecord = new HashMap<>();
        feedbackRecord.put("sessions_used", sessionData.getSessionsUsed());
        feedbackRecord.put("feedback", combinedDetails);
        feedbackRecord.put("type", "different");
        feedbackRecord.put("timestamp", TimeUtil.generateTimestamp());

        if (track.getCombinedFeedback() == null) {
            track.setCombinedFeedback(new ArrayList<>());
        }
        track.getCombinedFeedback().add(feedbackRecord);
        practiceGithubTrackRepository.save(track);

        return Map.of("sessions_used", sessionData.getSessionsUsed(), "feedback", combinedDetails);
    }

    // 16. Progress: Combined Feedback Within Same Session Reattempts
    @Transactional
    public Map<String, Object> combinedFeedbackSameSession(UserPrincipal principal, String githubIdStr,
            String sessionIdStr, int x) {
        if (x <= 1) {
            throw new CustomException("Invalid value of x", HttpStatus.BAD_REQUEST);
        }

        var verifiedGithub = githubVerificationService.verifyGithub(githubIdStr, principal.getId());
        PracticeGithubTrack track = verifiedGithub.getGithubTrack();
        var verifiedSession = githubVerificationService.verifyQuestionSession(sessionIdStr, track.getId());

        PreviousGithubSessionResponse sessionData = githubUtil.previousGithubSessionQuestions(
                track.getId(), x, verifiedSession.getSessionDoc().getSessionNumber());

        GithubAiResponses.CombinedFeedbackDetails combinedDetails = githubAiService
                .generateGithubCombinedSameSessionFeedback(
                        track.getRepoSummary(), new HashMap<>(sessionData.getSessionDict()));

        Map<String, Object> feedbackRecord = new HashMap<>();
        feedbackRecord.put("sessions_used", sessionData.getSessionsUsed());
        feedbackRecord.put("feedback", combinedDetails);
        feedbackRecord.put("type", "same");
        feedbackRecord.put("timestamp", TimeUtil.generateTimestamp());

        if (track.getCombinedFeedback() == null) {
            track.setCombinedFeedback(new ArrayList<>());
        }
        track.getCombinedFeedback().add(feedbackRecord);
        practiceGithubTrackRepository.save(track);

        return Map.of("sessions_used", sessionData.getSessionsUsed(), "feedback", combinedDetails);
    }

    // 17. Get Different Combined Feedback History
    public Map<String, Object> getDifferentCombinedFeedback(UserPrincipal principal, String githubIdStr) {
        var verifiedGithub = githubVerificationService.verifyGithub(githubIdStr, principal.getId());
        PracticeGithubTrack track = verifiedGithub.getGithubTrack();

        List<Map<String, Object>> result = new ArrayList<>();
        if (track.getCombinedFeedback() != null) {
            for (Map<String, Object> item : track.getCombinedFeedback()) {
                if ("different".equals(item.get("type"))) {
                    result.add(Map.of(
                            "sessions_used", item.get("sessions_used") != null ? item.get("sessions_used") : Map.of(),
                            "feedback", item.get("feedback") != null ? item.get("feedback") : "",
                            "type", item.get("type"),
                            "timestamp", item.get("timestamp") != null ? item.get("timestamp") : ""));
                }
            }
        }

        return Map.of("github_id", track.getId().toString(), "combined_feedback", result);
    }

    // 18. Get Same Combined Feedback History
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSameCombinedFeedback(UserPrincipal principal, String githubIdStr, int sessionNumber) {
        var verifiedGithub = githubVerificationService.verifyGithub(githubIdStr, principal.getId());
        PracticeGithubTrack track = verifiedGithub.getGithubTrack();

        List<Map<String, Object>> result = new ArrayList<>();
        if (track.getCombinedFeedback() != null) {
            for (Map<String, Object> item : track.getCombinedFeedback()) {
                if ("same".equals(item.get("type"))) {
                    Map<String, Object> sessionsUsed = (Map<String, Object>) item.get("sessions_used");
                    if (sessionsUsed != null && !sessionsUsed.isEmpty()) {
                        Object firstVal = sessionsUsed.values().iterator().next();
                        if (firstVal instanceof Map<?, ?> firstSessionMap) {
                            Object sNum = firstSessionMap.get("session_number");
                            if (sNum != null && Integer.parseInt(sNum.toString()) == sessionNumber) {
                                result.add(Map.of(
                                        "sessions_used", sessionsUsed,
                                        "feedback", item.get("feedback") != null ? item.get("feedback") : "",
                                        "type", item.get("type"),
                                        "timestamp", item.get("timestamp") != null ? item.get("timestamp") : ""));
                            }
                        }
                    }
                }
            }
        }

        return Map.of("github_id", track.getId().toString(), "combined_feedback", result);
    }

}