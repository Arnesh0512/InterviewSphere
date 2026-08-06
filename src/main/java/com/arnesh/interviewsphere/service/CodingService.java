package com.arnesh.interviewsphere.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arnesh.interviewsphere.ai.CodingAiService;
import com.arnesh.interviewsphere.constant.Difficulty;
import com.arnesh.interviewsphere.constant.Company;
import com.arnesh.interviewsphere.constant.Tag;
import com.arnesh.interviewsphere.dto.request.SaveAnswerRequest;
import com.arnesh.interviewsphere.dto.request.SubmitSessionRequest;
import com.arnesh.interviewsphere.dto.response.CodingAiResponses;
import com.arnesh.interviewsphere.dto.response.PreviousCodingSessionResponse;
import com.arnesh.interviewsphere.entity.Candidate;
import com.arnesh.interviewsphere.entity.LeetCodeQuestion;
import com.arnesh.interviewsphere.entity.PracticeCodingTrack;
import com.arnesh.interviewsphere.entity.QuestionSession;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.CandidateRepository;
import com.arnesh.interviewsphere.repository.LeetCodeQuestionRepository;
import com.arnesh.interviewsphere.repository.PracticeCodingTrackRepository;
import com.arnesh.interviewsphere.repository.QuestionSessionRepository;
import com.arnesh.interviewsphere.security.UserPrincipal;
import com.arnesh.interviewsphere.util.CodingUtil;
import com.arnesh.interviewsphere.util.TimeUtil;
import com.arnesh.interviewsphere.verification.CandidateVerificationService;
import com.arnesh.interviewsphere.verification.CodingVerificationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodingService {

    private final PracticeCodingTrackRepository practiceCodingTrackRepository;
    private final QuestionSessionRepository questionSessionRepository;
    private final LeetCodeQuestionRepository leetCodeQuestionRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateVerificationService candidateVerificationService;
    private final CodingVerificationService codingVerificationService;
    private final CodingAiService codingAiService;
    private final CodingUtil codingUtil;

    // 1. Start Coding Track
    @Transactional
    public Map<String, Object> startCoding(UserPrincipal principal, List<Company> company, List<Tag> tag, List<Difficulty> difficulty) {
        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        List<String> companyList = company != null ? company.stream().map(c -> c.getValue()).toList() : List.of();
        List<String> tagList = tag != null ? tag.stream().map(t -> t.getValue()).toList() : List.of();
        List<String> difficultyList = difficulty != null ? difficulty.stream().map(d -> d.getValue()).toList() : List.of();

        String[] compArray = companyList != null ? companyList.toArray(String[]::new) : new String[0];
        String[] tagArray = tagList != null ? tagList.toArray(String[]::new) : new String[0];
        String[] diffArray = difficultyList != null ? difficultyList.toArray(String[]::new) : new String[0];

        long availableQues = leetCodeQuestionRepository.countFilteredQuestions(
                compArray.length > 0, compArray,
                tagArray.length > 0, tagArray,
                diffArray.length > 0, diffArray
        );

        if (availableQues == 0) {
            return Map.of(
                    "success", false,
                    "message", "No questions found for given filters."
            );
        }

        int nextCodingNum = Objects.requireNonNullElse(candidate.getTotalCodings(), 0) + 1;
        

        PracticeCodingTrack track = PracticeCodingTrack.builder()
                .candidateId(candidate.getId())
                .codingNumber(nextCodingNum)
                .company(company != null ? companyList : List.of())
                .tag(tag != null ? tagList : List.of())
                .difficulty(difficulty != null ? difficultyList : List.of())
                .availableQues((int) availableQues)
                .totalSessions(0)
                .build();


        PracticeCodingTrack savedTrack = practiceCodingTrackRepository.save(track);

        candidate.setTotalCodings(nextCodingNum);
        candidateRepository.save(candidate);

        return Map.of("success", true,
        "coding_id", savedTrack.getId().toString());
    }

    // 2. Get All Coding Tracks
    public List<Map<String, Object>> getAllCodings(UserPrincipal principal) {
        List<PracticeCodingTrack> tracks = practiceCodingTrackRepository
                .findByCandidateIdOrderByCreatedOnDesc(principal.getId());

        return tracks.stream().map(track -> {
            Map<String, Object> map = new HashMap<>();
            map.put("coding_id", track.getId().toString());
            map.put("coding_number", track.getCodingNumber());
            map.put("company", track.getCompany());
            map.put("difficulty", track.getDifficulty());
            map.put("tag", track.getTag());
            map.put("available_ques", track.getAvailableQues());
            map.put("created_on", track.getCreatedOn());
            return map;
        }).toList();
    }

    // 3. Submit Session
    @Transactional
    public void submitSession(UserPrincipal principal, String codingIdStr, String sessionIdStr, SubmitSessionRequest request) {
        var verifiedCoding = codingVerificationService.verifyCoding(codingIdStr, principal.getId());
        var verifiedSession = codingVerificationService.verifyQuestionSession(sessionIdStr, verifiedCoding.getCodingId());
        QuestionSession session = verifiedSession.getSessionDoc();

        codingVerificationService.verifySessionStatus(session);
        codingVerificationService.verifySessionTime(session, session.getId());

        var verifiedTimestamp = codingVerificationService.verifyTimestamp(request.getFrontendTimestamp());

        session.setStatus("passive");
        session.setSubmittedAtFrontend(verifiedTimestamp.getFrontendTime());
        session.setSubmittedAtBackend(verifiedTimestamp.getBackendTime());

        questionSessionRepository.save(session);
    }

    // 4. Generate New Questions Session
    @Transactional
    public Map<String, Object> generateQuestions(UserPrincipal principal, String codingIdStr, int numQuestions) {
        var verifiedCoding = codingVerificationService.verifyCoding(codingIdStr, principal.getId());
        PracticeCodingTrack track = verifiedCoding.getCodingTrack();

        codingVerificationService.verifyQuantity(numQuestions, track);

        int sessionNumber = Objects.requireNonNullElse(track.getTotalSessions(), 0) + 1;

        List<Integer> excludeIdsInt = sessionNumber > 1
                ? codingUtil.getUsedCodingQuestionIds(track.getId())
                : List.of();

        String[] compArray = track.getCompany() != null ? track.getCompany().toArray(String[]::new) : new String[0];
        String[] tagArray = track.getTag() != null ? track.getTag().toArray(String[]::new) : new String[0];
        String[] diffArray = track.getDifficulty() != null ? track.getDifficulty().toArray(String[]::new) : new String[0];
        String[] excludeArray = excludeIdsInt.stream()
        .map(String::valueOf)
        .toArray(String[]::new);

        List<LeetCodeQuestion> questionsList = leetCodeQuestionRepository.findRandomFilteredQuestions(
                compArray.length > 0, compArray,
                tagArray.length > 0, tagArray,
                diffArray.length > 0, diffArray,
                excludeArray.length > 0, excludeArray,
                numQuestions
        );

        List<QuestionSession.Question> questionBank = new ArrayList<>();
        int time = numQuestions * 60; // 60 minutes per question

        for (LeetCodeQuestion q : questionsList) {
            // Change parsing to Integer:
            Integer qId = q.getQuestionId();

            // In questionBank builder:
            questionBank.add(QuestionSession.Question.builder()
                    .questionId(qId)
                    .language("")
                    .answer("")
                    .feedback("")
                    .score(0.0)
                    .build());
        }

        OffsetDateTime now = TimeUtil.generateTimestamp();
        QuestionSession session = QuestionSession.builder()
                .sessionNumber(sessionNumber)
                .codingTrackId(track.getId())
                .time(time)
                .questionBank(questionBank)
                .status("active")
                .timestamp(now)
                .build();

        questionSessionRepository.save(session);

        track.setTotalSessions(sessionNumber);
        track.setAvailableQues(track.getAvailableQues() - numQuestions);
        practiceCodingTrackRepository.save(track);

        List<Map<String, Object>> formattedQuestions = questionsList.stream().map(q -> {
            Map<String, Object> m = new HashMap<>();
            m.put("question_id", q.getQuestionId());
            m.put("task_name", q.getTaskName());
            m.put("problem_description", q.getProblemDescription());
            return m;
        }).toList();

        return Map.of(
                "success", true,
                "coding_id", track.getId().toString(),
                "question_session_id", session.getId().toString(),
                "time", time,
                "questions", formattedQuestions
        );
    }

    // 5. Save Single Answer
    @Transactional
    public void saveAnswer(UserPrincipal principal, String codingIdStr, String sessionIdStr, SaveAnswerRequest request) {
        var verifiedCoding = codingVerificationService.verifyCoding(codingIdStr, principal.getId());
        var verifiedSession = codingVerificationService.verifyQuestionSession(sessionIdStr, verifiedCoding.getCodingId());
        QuestionSession session = verifiedSession.getSessionDoc();

        codingVerificationService.verifySessionStatus(session);
        codingVerificationService.verifySessionTime(session, session.getId());
        codingVerificationService.verifyQuestionId(session, request.getQuestionId());

        if (session.getQuestionBank() != null) {
            for (QuestionSession.Question q : session.getQuestionBank()) {
                if (q.getQuestionId() != null && q.getQuestionId().equals(request.getQuestionId())) {
                    q.setLanguage(request.getLanguage());
                    q.setAnswer(request.getAnswer());
                    break;
                }
            }
        }

        questionSessionRepository.save(session);
    }

    // 6. Reattempt Question Session
    @Transactional
    public Map<String, String> reattemptSession(UserPrincipal principal, String codingIdStr, String sessionIdStr) {
        var verifiedCoding = codingVerificationService.verifyCoding(codingIdStr, principal.getId());
        var verifiedSession = codingVerificationService.verifyQuestionSession(sessionIdStr, verifiedCoding.getCodingId());
        QuestionSession oldSession = verifiedSession.getSessionDoc();

        List<QuestionSession.Question> newQuestionBank = new ArrayList<>();
        if (oldSession.getQuestionBank() != null) {
            for (QuestionSession.Question q : oldSession.getQuestionBank()) {
                newQuestionBank.add(QuestionSession.Question.builder()
                        .questionId(q.getQuestionId())
                        .language("")
                        .answer("")
                        .feedback("")
                        .score(0.0)
                        .build());
            }
        }

        QuestionSession newSession = QuestionSession.builder()
                .sessionNumber(oldSession.getSessionNumber())
                .codingTrackId(verifiedCoding.getCodingId())
                .time(oldSession.getTime())
                .questionBank(newQuestionBank)
                .status("active")
                .timestamp(TimeUtil.generateTimestamp())
                .build();

        questionSessionRepository.save(newSession);

        return Map.of("new_question_session_id", newSession.getId().toString());
    }

    // 7. Fake Submit Session
    @Transactional
    public void fakeSubmitSession(UserPrincipal principal, String codingIdStr, String sessionIdStr) {
        var verifiedCoding = codingVerificationService.verifyCoding(codingIdStr, principal.getId());
        var verifiedSession = codingVerificationService.verifyQuestionSession(sessionIdStr, verifiedCoding.getCodingId());
        QuestionSession session = verifiedSession.getSessionDoc();

        codingVerificationService.verifySessionStatus(session);

        OffsetDateTime timestamp = session.getTimestamp() != null ? session.getTimestamp() : TimeUtil.generateTimestamp();
        int minutes = Objects.requireNonNullElse(session.getTime(), 0);
        OffsetDateTime autoSubmitTime = timestamp.plusMinutes(minutes + 1);

        session.setStatus("passive");
        session.setSubmittedAtFrontend(autoSubmitTime);
        session.setSubmittedAtBackend(autoSubmitTime);

        questionSessionRepository.save(session);
    }

    // 8. Generate AI Feedback
    @Transactional
    public CodingAiResponses.CodingEvaluationResponse generateFeedback(UserPrincipal principal, String codingIdStr, String sessionIdStr) {
        var verifiedCoding = codingVerificationService.verifyCoding(codingIdStr, principal.getId());
        var verifiedSession = codingVerificationService.verifyQuestionSession(sessionIdStr, verifiedCoding.getCodingId());
        QuestionSession session = verifiedSession.getSessionDoc();

        codingVerificationService.verifySessionStatus2(session);

        List<Integer> questionIds = new ArrayList<>();
        if (session.getQuestionBank() != null) {
            for (QuestionSession.Question q : session.getQuestionBank()) {
                if (q.getQuestionId() != null) {
                    questionIds.add(q.getQuestionId());
                }
            }
        }

        List<LeetCodeQuestion> questionDocs = leetCodeQuestionRepository.findByQuestionIdIn(questionIds);

        Map<Integer, String> questionMap = new HashMap<>();
        for (LeetCodeQuestion doc : questionDocs) {
            questionMap.put(doc.getQuestionId(), doc.getProblemDescription());
        }

        List<Map<String, Object>> enrichedQuestions = new ArrayList<>();
        if (session.getQuestionBank() != null) {
            for (QuestionSession.Question q : session.getQuestionBank()) {
                Integer qid = Objects.requireNonNullElse(q.getQuestionId(),0);
                enrichedQuestions.add(Map.of(
                        "question_id", qid,
                        "problem_description", questionMap.getOrDefault(qid, ""),
                        "language", q.getLanguage() != null ? q.getLanguage() : "",
                        "answer", q.getAnswer() != null ? q.getAnswer() : ""
                ));
            }
        }


        // Pass enrichedQuestions directly without the (List) cast
        CodingAiResponses.CodingEvaluationResponse evaluation = codingAiService.evaluateCodingAnswers(enrichedQuestions);

        if (evaluation.getFeedbackPerQuestion() != null) {
            Map<Integer, CodingAiResponses.QuestionFeedback> feedbackMap = new HashMap<>();
            for (var item : evaluation.getFeedbackPerQuestion()) {
                if (item.getQuestionId() != null) {
                    feedbackMap.put(item.getQuestionId(), item);
                }
            }

            for (QuestionSession.Question q : session.getQuestionBank()) {
                Integer qid = Objects.requireNonNullElse(q.getQuestionId(), 0);
                if (feedbackMap.containsKey(qid)) {
                    q.setFeedback(feedbackMap.get(qid).getFeedback());
                    q.setScore(feedbackMap.get(qid).getScore());
                }
            }
        }

        session.setOverallFeedback(evaluation.getOverallFeedback());
        session.setOverallScore(evaluation.getOverallScore());
        questionSessionRepository.save(session);

        return evaluation;
    }

    // 9. Get All Sessions
    public Map<String, Object> getAllSessions(UserPrincipal principal, String codingIdStr) {
        var verifiedCoding = codingVerificationService.verifyCoding(codingIdStr, principal.getId());
        List<QuestionSession> sessions = questionSessionRepository
                .findByCodingTrackId(verifiedCoding.getCodingId());

        List<Map<String, Object>> sessionList = sessions.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("question_session_id", s.getId().toString());
            m.put("session_number", s.getSessionNumber());
            m.put("status", s.getStatus());
            m.put("timestamp", s.getTimestamp());
            return m;
        }).toList();

        return Map.of("coding_id", verifiedCoding.getCodingId().toString(), "sessions", sessionList);
    }

    // 10. Get Single Session Data
    public Map<String, Object> getSessionData(UserPrincipal principal, String codingIdStr, String sessionIdStr) {
        var verifiedCoding = codingVerificationService.verifyCoding(codingIdStr, principal.getId());
        var verifiedSession = codingVerificationService.verifyQuestionSession(sessionIdStr, verifiedCoding.getCodingId());

        return Map.of("coding_id", verifiedCoding.getCodingId().toString(), "session", verifiedSession.getSessionDoc());
    }

    // 11. Delete Coding Track
    @Transactional
    public void deleteCoding(UserPrincipal principal, String codingIdStr) {
        var verifiedCoding = codingVerificationService.verifyCoding(codingIdStr, principal.getId());
        questionSessionRepository.deleteByCodingTrackId(verifiedCoding.getCodingId());
        practiceCodingTrackRepository.deleteById(verifiedCoding.getCodingId());
    }

    // 12. Delete Single Session
    @Transactional
    public void deleteSession(UserPrincipal principal, String codingIdStr, String sessionIdStr) {
        var verifiedCoding = codingVerificationService.verifyCoding(codingIdStr, principal.getId());
        var verifiedSession = codingVerificationService.verifyQuestionSession(sessionIdStr, verifiedCoding.getCodingId());

        long sameSessionCount = questionSessionRepository.countByCodingTrackIdAndSessionNumber(
                verifiedCoding.getCodingId(), verifiedSession.getSessionDoc().getSessionNumber());

        if (sameSessionCount <= 1) {
            throw new CustomException("Can't delete complete session entirely, either reattempt or leave.", HttpStatus.BAD_REQUEST);
        }

        questionSessionRepository.deleteById(verifiedSession.getSessionObjId());
    }

    // 13. Delete Answers and Reattempt
    @Transactional
    public void deleteAndReattempt(UserPrincipal principal, String codingIdStr, String sessionIdStr) {
        var verifiedCoding = codingVerificationService.verifyCoding(codingIdStr, principal.getId());
        var verifiedSession = codingVerificationService.verifyQuestionSession(sessionIdStr, verifiedCoding.getCodingId());
        QuestionSession session = verifiedSession.getSessionDoc();

        if (session.getQuestionBank() != null) {
            for (QuestionSession.Question q : session.getQuestionBank()) {
                q.setLanguage("");
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

    // 14. Progress: Combined Feedback Across Different Sessions
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, Object> combinedFeedbackLastXSessions(UserPrincipal principal, String codingIdStr, int x) {
        if (x <= 1) {
            throw new CustomException("Invalid value of x", HttpStatus.BAD_REQUEST);
        }

        var verifiedCoding = codingVerificationService.verifyCoding(codingIdStr, principal.getId());
        PracticeCodingTrack track = verifiedCoding.getCodingTrack();

        PreviousCodingSessionResponse sessionData = codingUtil.previousCodingSessionQuestions(track.getId(), x, null);
        CodingAiResponses.CombinedFeedbackDetails combinedDetails = codingAiService.generateCodingCombinedDiffSessionFeedback((Map) sessionData.getSessionDict());

        Map<String, Object> feedbackRecord = new HashMap<>();
        feedbackRecord.put("sessions_used", sessionData.getSessionsUsed());
        feedbackRecord.put("feedback", combinedDetails);
        feedbackRecord.put("type", "different");
        feedbackRecord.put("timestamp", TimeUtil.generateTimestamp());

        if (track.getCombinedFeedback() == null) {
            track.setCombinedFeedback(new ArrayList<>());
        }
        track.getCombinedFeedback().add(feedbackRecord);
        practiceCodingTrackRepository.save(track);

        return Map.of("sessions_used", sessionData.getSessionsUsed(), "feedback", combinedDetails);
    }

    // 15. Progress: Combined Feedback Within Same Session Reattempts
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, Object> combinedFeedbackSameSession(UserPrincipal principal, String codingIdStr, String sessionIdStr, int x) {
        if (x <= 1) {
            throw new CustomException("Invalid value of x", HttpStatus.BAD_REQUEST);
        }

        var verifiedCoding = codingVerificationService.verifyCoding(codingIdStr, principal.getId());
        PracticeCodingTrack track = verifiedCoding.getCodingTrack();
        var verifiedSession = codingVerificationService.verifyQuestionSession(sessionIdStr, track.getId());

        PreviousCodingSessionResponse sessionData = codingUtil.previousCodingSessionQuestions(
                track.getId(), x, verifiedSession.getSessionDoc().getSessionNumber());

        CodingAiResponses.CombinedFeedbackDetails combinedDetails = codingAiService.generateCodingCombinedSameSessionFeedback((Map) sessionData.getSessionDict());

        Map<String, Object> feedbackRecord = new HashMap<>();
        feedbackRecord.put("sessions_used", sessionData.getSessionsUsed());
        feedbackRecord.put("feedback", combinedDetails);
        feedbackRecord.put("type", "same");
        feedbackRecord.put("timestamp", TimeUtil.generateTimestamp());

        if (track.getCombinedFeedback() == null) {
            track.setCombinedFeedback(new ArrayList<>());
        }
        track.getCombinedFeedback().add(feedbackRecord);
        practiceCodingTrackRepository.save(track);

        return Map.of("sessions_used", sessionData.getSessionsUsed(), "feedback", combinedDetails);
    }

    // 16. Get Different Combined Feedback History
    public Map<String, Object> getDifferentCombinedFeedback(UserPrincipal principal, String codingIdStr) {
        var verifiedCoding = codingVerificationService.verifyCoding(codingIdStr, principal.getId());
        PracticeCodingTrack track = verifiedCoding.getCodingTrack();

        List<Map<String, Object>> result = new ArrayList<>();
        if (track.getCombinedFeedback() != null) {
            for (Map<String, Object> item : track.getCombinedFeedback()) {
                if ("different".equals(item.get("type"))) {
                    result.add(Map.of(
                            "sessions_used", item.get("sessions_used") != null ? item.get("sessions_used") : Map.of(),
                            "feedback", item.get("feedback") != null ? item.get("feedback") : "",
                            "type", item.get("type"),
                            "timestamp", item.get("timestamp") != null ? item.get("timestamp") : ""
                    ));
                }
            }
        }

        return Map.of("coding_id", track.getId().toString(), "combined_feedback", result);
    }

    // 17. Get Same Combined Feedback History
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSameCombinedFeedback(UserPrincipal principal, String codingIdStr, int sessionNumber) {
        var verifiedCoding = codingVerificationService.verifyCoding(codingIdStr, principal.getId());
        PracticeCodingTrack track = verifiedCoding.getCodingTrack();

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
                                        "timestamp", item.get("timestamp") != null ? item.get("timestamp") : ""
                                ));
                            }
                        }
                    }
                }
            }
        }

        return Map.of("coding_id", track.getId().toString(), "combined_feedback", result);
    }
}