package com.arnesh.interviewsphere.service;

import com.arnesh.interviewsphere.ai.ConceptAiService;
import com.arnesh.interviewsphere.constant.Topic;
import com.arnesh.interviewsphere.dto.request.SaveAnswerRequest;
import com.arnesh.interviewsphere.dto.request.SubmitSessionRequest;
import com.arnesh.interviewsphere.dto.response.ConceptAiResponses;
import com.arnesh.interviewsphere.dto.response.PreviousConceptSessionResponse;
import com.arnesh.interviewsphere.dto.response.QuestionSessionResponse;
import com.arnesh.interviewsphere.entity.Candidate;
import com.arnesh.interviewsphere.entity.PracticeConceptTrack;
import com.arnesh.interviewsphere.entity.QuestionSession;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.CandidateRepository;
import com.arnesh.interviewsphere.repository.PracticeConceptTrackRepository;
import com.arnesh.interviewsphere.repository.QuestionSessionRepository;
import com.arnesh.interviewsphere.security.UserPrincipal;
import com.arnesh.interviewsphere.util.ConceptUtil;
import com.arnesh.interviewsphere.util.TimeUtil;
import com.arnesh.interviewsphere.verification.CandidateVerificationService;
import com.arnesh.interviewsphere.verification.ConceptVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConceptService {

    private final PracticeConceptTrackRepository practiceConceptTrackRepository;
    private final QuestionSessionRepository questionSessionRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateVerificationService candidateVerificationService;
    private final ConceptVerificationService conceptVerificationService;
    private final ConceptAiService conceptAiService;
    private final ConceptUtil conceptUtil;

    // 1. Start Concept Track
    @Transactional
    public Map<String, Object> startConcept(UserPrincipal principal, List<Topic> topics) {
        if (topics == null || topics.isEmpty()) {
            throw new CustomException("At least one topic must be selected", HttpStatus.BAD_REQUEST);
        }

        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        int nextConceptNum = (candidate.getTotalConcepts() != null ? candidate.getTotalConcepts() : 0) + 1;

        PracticeConceptTrack track = PracticeConceptTrack.builder()
                .candidateId(candidate.getId())
                .conceptNumber(nextConceptNum)
                .topics(topics)
                .totalSessions(0)
                .build();

        practiceConceptTrackRepository.save(track);

        candidate.setTotalConcepts(nextConceptNum);
        candidateRepository.save(candidate);

        return Map.of("success", true);
    }

    // 2. Get All Concept Tracks
    public List<Map<String, Object>> getAllConcepts(UserPrincipal principal) {
        List<PracticeConceptTrack> tracks = practiceConceptTrackRepository
                .findByCandidateIdOrderByCreatedOnDesc(principal.getId());

        return tracks.stream().map(track -> {
            Map<String, Object> map = new HashMap<>();
            map.put("concept_id", track.getId().toString());
            map.put("concept_number", track.getConceptNumber());
            map.put("topic", track.getTopics());
            map.put("created_on", track.getCreatedOn());
            return map;
        }).toList();
    }

    // 3. Submit Session
    @Transactional
    public void submitSession(UserPrincipal principal, String conceptIdStr, String sessionIdStr, SubmitSessionRequest request) {
        var verifiedConcept = conceptVerificationService.verifyConcept(conceptIdStr, principal.getId());
        var verifiedSession = conceptVerificationService.verifyQuestionSession(sessionIdStr, verifiedConcept.getConceptId());
        QuestionSession session = verifiedSession.getSessionDoc();

        conceptVerificationService.verifySessionStatus(session);
        conceptVerificationService.verifySessionTime(session, session.getId());

        var verifiedTimestamp = conceptVerificationService.verifyTimestamp(request.getFrontendTimestamp());

        session.setStatus("passive");
        session.setSubmittedAtFrontend(verifiedTimestamp.getFrontendTime());
        session.setSubmittedAtBackend(verifiedTimestamp.getBackendTime());

        questionSessionRepository.save(session);
    }

    // 4. Generate New Questions Session
    @Transactional
    public QuestionSessionResponse generateQuestions(UserPrincipal principal, String conceptIdStr, int numQuestions) {
        var verifiedConcept = conceptVerificationService.verifyConcept(conceptIdStr, principal.getId());
        PracticeConceptTrack track = verifiedConcept.getConceptTrack();

        int sessionNumber = (track.getTotalSessions() != null ? track.getTotalSessions() : 0) + 1;

        Map<String, Map<String, String>> previousSessions = new HashMap<>();
        if (sessionNumber != 1) {
            PreviousConceptSessionResponse prevResponse = conceptUtil.previousConceptSessionQuestions(track.getId(), null, null);
            previousSessions = prevResponse.getSessionDict();
        }

        List<String> topicStrings = track.getTopics().stream().map(t -> t.name()).toList();
        ConceptAiResponses.QuestionListResponse aiQuestions = conceptAiService.generateConceptTopicQuestions(
                topicStrings, numQuestions, previousSessions);

        List<QuestionSession.Question> questionBank = new ArrayList<>();
        int durationMinutes = numQuestions * 10;

        for (int i = 0; i < aiQuestions.getQuestions().size(); i++) {
            questionBank.add(QuestionSession.Question.builder()
                    .questionId(i + 1)
                    .question(aiQuestions.getQuestions().get(i))
                    .answer("")
                    .feedback("")
                    .score(0.0)
                    .build());
        }

        OffsetDateTime now = TimeUtil.generateTimestamp();
        QuestionSession session = QuestionSession.builder()
                .sessionNumber(sessionNumber)
                .conceptTrackId(track.getId())
                .time(durationMinutes)
                .questionBank(questionBank)
                .status("active")
                .timestamp(now)
                .build();

        questionSessionRepository.save(session);

        track.setTotalSessions(sessionNumber);
        practiceConceptTrackRepository.save(track);

        return QuestionSessionResponse.builder()
                .sessionId(session.getId().toString())
                .sessionNumber(sessionNumber)
                .status("active")
                .time(durationMinutes)
                .timestamp(now)
                .build();
    }

    // 5. Save Single Answer
    @Transactional
    public void saveAnswer(UserPrincipal principal, String conceptIdStr, String sessionIdStr, SaveAnswerRequest request) {
        var verifiedConcept = conceptVerificationService.verifyConcept(conceptIdStr, principal.getId());
        var verifiedSession = conceptVerificationService.verifyQuestionSession(sessionIdStr, verifiedConcept.getConceptId());
        QuestionSession session = verifiedSession.getSessionDoc();

        conceptVerificationService.verifySessionStatus(session);
        conceptVerificationService.verifySessionTime(session, session.getId());
        conceptVerificationService.verifyQuestionId(session, request.getQuestionId());

        if (session.getQuestionBank() != null) {
            for (QuestionSession.Question q : session.getQuestionBank()) {
                if (Objects.equals(q.getQuestionId(), request.getQuestionId())) {
                    q.setAnswer(request.getAnswer());
                    break;
                }
            }
        }

        questionSessionRepository.save(session);
    }

    // 6. Reattempt Session
    @Transactional
    public Map<String, String> reattemptSession(UserPrincipal principal, String conceptIdStr, String sessionIdStr) {
        var verifiedConcept = conceptVerificationService.verifyConcept(conceptIdStr, principal.getId());
        var verifiedSession = conceptVerificationService.verifyQuestionSession(sessionIdStr, verifiedConcept.getConceptId());
        QuestionSession oldSession = verifiedSession.getSessionDoc();

        List<QuestionSession.Question> newQuestionBank = new ArrayList<>();
        if (oldSession.getQuestionBank() != null) {
            for (QuestionSession.Question q : oldSession.getQuestionBank()) {
                newQuestionBank.add(QuestionSession.Question.builder()
                        .questionId(q.getQuestionId())
                        .question(q.getQuestion())
                        .answer("")
                        .feedback("")
                        .score(0.0)
                        .build());
            }
        }

        QuestionSession newSession = QuestionSession.builder()
                .sessionNumber(oldSession.getSessionNumber())
                .conceptTrackId(verifiedConcept.getConceptId())
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
    public void fakeSubmitSession(UserPrincipal principal, String conceptIdStr, String sessionIdStr) {
        var verifiedConcept = conceptVerificationService.verifyConcept(conceptIdStr, principal.getId());
        var verifiedSession = conceptVerificationService.verifyQuestionSession(sessionIdStr, verifiedConcept.getConceptId());
        QuestionSession session = verifiedSession.getSessionDoc();

        conceptVerificationService.verifySessionStatus(session);

        OffsetDateTime timestamp = session.getTimestamp() != null ? session.getTimestamp() : TimeUtil.generateTimestamp();
        int minutes = session.getTime() != null ? session.getTime() : 0;
        OffsetDateTime autoSubmitTime = timestamp.plusMinutes(minutes + 1);

        session.setStatus("passive");
        session.setSubmittedAtFrontend(autoSubmitTime);
        session.setSubmittedAtBackend(autoSubmitTime);

        questionSessionRepository.save(session);
    }

    // 8. Generate AI Feedback
    @Transactional
    public ConceptAiResponses.ConceptEvaluationResponse generateFeedback(UserPrincipal principal, String conceptIdStr, String sessionIdStr) {
        var verifiedConcept = conceptVerificationService.verifyConcept(conceptIdStr, principal.getId());
        var verifiedSession = conceptVerificationService.verifyQuestionSession(sessionIdStr, verifiedConcept.getConceptId());
        QuestionSession session = verifiedSession.getSessionDoc();

        conceptVerificationService.verifySessionStatus2(session);

        List<String> topicStrings = verifiedConcept.getConceptTrack().getTopics().stream().map(t -> t.name()).toList();
        
        // Pass session.getQuestionBank() directly without raw (List) casting
        ConceptAiResponses.ConceptEvaluationResponse evaluation = conceptAiService.evaluateConceptTopicAnswers(
                topicStrings, session.getQuestionBank());

        if (evaluation.getFeedbackPerQuestion() != null) {
            Map<Integer, ConceptAiResponses.QuestionFeedback> feedbackMap = new HashMap<>();
            for (var item : evaluation.getFeedbackPerQuestion()) {
                feedbackMap.put(item.getQuestionId(), item);
            }

            for (QuestionSession.Question q : session.getQuestionBank()) {
                if (feedbackMap.containsKey(q.getQuestionId())) {
                    q.setFeedback(feedbackMap.get(q.getQuestionId()).getFeedback());
                    q.setScore(feedbackMap.get(q.getQuestionId()).getScore());
                }
            }
        }

        session.setOverallFeedback(evaluation.getOverallFeedback());
        session.setOverallScore(evaluation.getOverallScore());
        questionSessionRepository.save(session);

        return evaluation;
    }

    // 9. Get All Sessions
    public Map<String, Object> getAllSessions(UserPrincipal principal, String conceptIdStr) {
        var verifiedConcept = conceptVerificationService.verifyConcept(conceptIdStr, principal.getId());
        List<QuestionSession> sessions = questionSessionRepository
                .findByConceptTrackIdOrderByTimestampAsc(verifiedConcept.getConceptId());

        List<Map<String, Object>> sessionList = sessions.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("question_session_id", s.getId().toString());
            m.put("session_number", s.getSessionNumber());
            m.put("status", s.getStatus());
            m.put("timestamp", s.getTimestamp());
            return m;
        }).toList();

        return Map.of("concept_id", verifiedConcept.getConceptId().toString(), "sessions", sessionList);
    }

    // 10. Get Single Session Data
    public Map<String, Object> getSessionData(UserPrincipal principal, String conceptIdStr, String sessionIdStr) {
        var verifiedConcept = conceptVerificationService.verifyConcept(conceptIdStr, principal.getId());
        var verifiedSession = conceptVerificationService.verifyQuestionSession(sessionIdStr, verifiedConcept.getConceptId());

        return Map.of("concept_id", verifiedConcept.getConceptId().toString(), "session", verifiedSession.getSessionDoc());
    }

    // 11. Delete Concept Track
    @Transactional
    public void deleteConcept(UserPrincipal principal, String conceptIdStr) {
        var verifiedConcept = conceptVerificationService.verifyConcept(conceptIdStr, principal.getId());
        questionSessionRepository.deleteByConceptTrackId(verifiedConcept.getConceptId());
        practiceConceptTrackRepository.deleteById(verifiedConcept.getConceptId());
    }

    // 12. Delete Single Session
    @Transactional
    public void deleteSession(UserPrincipal principal, String conceptIdStr, String sessionIdStr) {
        var verifiedConcept = conceptVerificationService.verifyConcept(conceptIdStr, principal.getId());
        var verifiedSession = conceptVerificationService.verifyQuestionSession(sessionIdStr, verifiedConcept.getConceptId());

        long sameSessionCount = questionSessionRepository.countByConceptTrackIdAndSessionNumber(
                verifiedConcept.getConceptId(), verifiedSession.getSessionDoc().getSessionNumber());

        if (sameSessionCount <= 1) {
            throw new CustomException("Can't delete complete session entirely, either reattempt or leave.", HttpStatus.BAD_REQUEST);
        }

        questionSessionRepository.deleteById(verifiedSession.getSessionObjId());
    }

    // 13. Delete Answers and Reattempt
    @Transactional
    public void deleteAndReattempt(UserPrincipal principal, String conceptIdStr, String sessionIdStr) {
        var verifiedConcept = conceptVerificationService.verifyConcept(conceptIdStr, principal.getId());
        var verifiedSession = conceptVerificationService.verifyQuestionSession(sessionIdStr, verifiedConcept.getConceptId());
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

    // 14. Progress: Combined Feedback Across Different Sessions
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, Object> combinedFeedbackLastXSessions(UserPrincipal principal, String conceptIdStr, int x) {
        if (x <= 1) {
            throw new CustomException("Invalid value of x", HttpStatus.BAD_REQUEST);
        }

        var verifiedConcept = conceptVerificationService.verifyConcept(conceptIdStr, principal.getId());
        PracticeConceptTrack track = verifiedConcept.getConceptTrack();

        PreviousConceptSessionResponse sessionData = conceptUtil.previousConceptSessionQuestions(track.getId(), x, null);
        List<String> topicStrings = track.getTopics().stream().map(t -> t.name()).toList();

        ConceptAiResponses.CombinedFeedbackDetails combinedDetails = conceptAiService.generateConceptCombinedDiffSessionFeedback(
                topicStrings, (Map) sessionData.getSessionDict());

        Map<String, Object> feedbackRecord = new HashMap<>();
        feedbackRecord.put("sessions_used", sessionData.getSessionsUsed());
        feedbackRecord.put("feedback", combinedDetails);
        feedbackRecord.put("type", "different");
        feedbackRecord.put("timestamp", TimeUtil.generateTimestamp());

        if (track.getCombinedFeedback() == null) {
            track.setCombinedFeedback(new ArrayList<>());
        }
        track.getCombinedFeedback().add(feedbackRecord);
        practiceConceptTrackRepository.save(track);

        return Map.of("sessions_used", sessionData.getSessionsUsed(), "feedback", combinedDetails);
    }

    // 15. Progress: Combined Feedback Within Same Session Reattempts
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, Object> combinedFeedbackSameSession(UserPrincipal principal, String conceptIdStr, String sessionIdStr, int x) {
        if (x <= 1) {
            throw new CustomException("Invalid value of x", HttpStatus.BAD_REQUEST);
        }

        var verifiedConcept = conceptVerificationService.verifyConcept(conceptIdStr, principal.getId());
        PracticeConceptTrack track = verifiedConcept.getConceptTrack();
        var verifiedSession = conceptVerificationService.verifyQuestionSession(sessionIdStr, track.getId());

        PreviousConceptSessionResponse sessionData = conceptUtil.previousConceptSessionQuestions(
                track.getId(), x, verifiedSession.getSessionDoc().getSessionNumber());
        List<String> topicStrings = track.getTopics().stream().map(t -> t.name()).toList();

        ConceptAiResponses.CombinedFeedbackDetails combinedDetails = conceptAiService.generateConceptCombinedSameSessionFeedback(
                topicStrings, (Map) sessionData.getSessionDict());

        Map<String, Object> feedbackRecord = new HashMap<>();
        feedbackRecord.put("sessions_used", sessionData.getSessionsUsed());
        feedbackRecord.put("feedback", combinedDetails);
        feedbackRecord.put("type", "same");
        feedbackRecord.put("timestamp", TimeUtil.generateTimestamp());

        if (track.getCombinedFeedback() == null) {
            track.setCombinedFeedback(new ArrayList<>());
        }
        track.getCombinedFeedback().add(feedbackRecord);
        practiceConceptTrackRepository.save(track);

        return Map.of("sessions_used", sessionData.getSessionsUsed(), "feedback", combinedDetails);
    }

    // 16. Get Different Combined Feedback History
    public Map<String, Object> getDifferentCombinedFeedback(UserPrincipal principal, String conceptIdStr) {
        var verifiedConcept = conceptVerificationService.verifyConcept(conceptIdStr, principal.getId());
        PracticeConceptTrack track = verifiedConcept.getConceptTrack();

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

        return Map.of("concept_id", track.getId().toString(), "combined_feedback", result);
    }

    // 17. Get Same Combined Feedback History
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSameCombinedFeedback(UserPrincipal principal, String conceptIdStr, int sessionNumber) {
        var verifiedConcept = conceptVerificationService.verifyConcept(conceptIdStr, principal.getId());
        PracticeConceptTrack track = verifiedConcept.getConceptTrack();

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

        return Map.of("concept_id", track.getId().toString(), "combined_feedback", result);
    }
}