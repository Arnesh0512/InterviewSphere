package com.arnesh.interviewsphere.service;

import com.arnesh.interviewsphere.ai.ResumeAiService;
import com.arnesh.interviewsphere.dto.request.SaveAnswerRequest;
import com.arnesh.interviewsphere.dto.request.SubmitSessionRequest;
import com.arnesh.interviewsphere.dto.response.PreviousResumeSessionResponse;
import com.arnesh.interviewsphere.dto.response.QuestionSessionResponse;
import com.arnesh.interviewsphere.dto.response.ResumeAiResponses;
import com.arnesh.interviewsphere.entity.Candidate;
import com.arnesh.interviewsphere.entity.PracticeResumeTrack;
import com.arnesh.interviewsphere.entity.QuestionSession;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.CandidateRepository;
import com.arnesh.interviewsphere.repository.PracticeResumeTrackRepository;
import com.arnesh.interviewsphere.repository.QuestionSessionRepository;
import com.arnesh.interviewsphere.security.UserPrincipal;
import com.arnesh.interviewsphere.util.ResumeUtil;
import com.arnesh.interviewsphere.util.TimeUtil;
import com.arnesh.interviewsphere.verification.CandidateVerificationService;
import com.arnesh.interviewsphere.verification.ResumeVerificationService;
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
public class ResumeService {

    private final PracticeResumeTrackRepository practiceResumeTrackRepository;
    private final QuestionSessionRepository questionSessionRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateVerificationService candidateVerificationService;
    private final ResumeVerificationService resumeVerificationService;
    private final ResumeAiService resumeAiService;
    private final ResumeUtil resumeUtil;
    private final SupabaseStorageService supabaseStorageService;

    // 1. Upload Resume
    @Transactional
    public Map<String, Object> uploadResume(UserPrincipal principal, MultipartFile file, String ocrMode) {
        if (file.getContentType() == null || !file.getContentType().equalsIgnoreCase("application/pdf")) {
            throw new CustomException("Only PDF allowed", HttpStatus.BAD_REQUEST);
        }

        var candidateResult = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = candidateResult.getCandidate();

        // 1. Upload to Supabase Storage
        String fileUrl = supabaseStorageService.uploadFile("resumes", file);

        File tempFile;
        try {
            tempFile = File.createTempFile("resume_", ".pdf");
            file.transferTo(tempFile);
            tempFile.deleteOnExit();
        } catch (IOException e) {
            throw new CustomException("Failed to process file locally", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Inside ResumeService.java, update the uploadResume method's text processing block:

        String summary;
        try {
            // Let ResumeAiService handle text extraction (OCR or without OCR) AND LLM summarization
            summary = resumeAiService.processResume(tempFile.getAbsolutePath(), ocrMode);
            
            // Sanitize generated summary to remove null bytes (0x00) which break Postgres UTF-8 encoding
            if (summary != null) {
                summary = summary.replace("\0", "");
            }
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }

        int nextResumeNum = (candidate.getTotalResumes() != null ? candidate.getTotalResumes() : 0) + 1;

        PracticeResumeTrack track = PracticeResumeTrack.builder()
                .candidateId(candidate.getId())
                .resumeNumber(nextResumeNum)
                .summary(summary)
                .filename(file.getOriginalFilename())
                .fileUrl(fileUrl) // Replaced fileId with fileUrl
                .createdOn(TimeUtil.generateTimestamp())
                .totalSessions(0)
                .build();

        PracticeResumeTrack savedTrack = practiceResumeTrackRepository.save(track);

        candidate.setTotalResumes(nextResumeNum);
        candidateRepository.save(candidate);

        return Map.of("success", true,
        "resume_id", savedTrack.getId().toString(),
         "file_url", fileUrl);
    }

    // 2. Get All Resumes
    public List<Map<String, Object>> getAllResumes(UserPrincipal principal) {
        List<PracticeResumeTrack> tracks = practiceResumeTrackRepository
                .findByCandidateIdOrderByCreatedOnDesc(principal.getId());

        return tracks.stream().map(track -> {
            Map<String, Object> map = new HashMap<>();
            map.put("resume_id", track.getId().toString());
            map.put("resume_number", track.getResumeNumber());
            map.put("file_url", track.getFileUrl()); // Exposed URL directly
            map.put("filename", track.getFilename());
            map.put("created_on", track.getCreatedOn());
            return map;
        }).toList();
    }

    // 3. Get Resume Details / File Metadata
    public PracticeResumeTrack getResumeTrack(UserPrincipal principal, String resumeIdStr) {
        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        return verifiedTrack.getResumeTrack();
    }

    // 4. Submit Session
    @Transactional
    public void submitSession(UserPrincipal principal, String resumeIdStr, String sessionIdStr,
            SubmitSessionRequest request) {
        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        var verifiedSession = resumeVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedTrack.getResumeId());
        QuestionSession session = verifiedSession.getSessionDoc();

        resumeVerificationService.verifySessionStatus(session);
        resumeVerificationService.verifySessionTime(session, session.getId());

        var verifiedTimestamp = resumeVerificationService.verifyTimestamp(request.getFrontendTimestamp());

        session.setStatus("passive");
        session.setSubmittedAtFrontend(verifiedTimestamp.getFrontendTime());
        session.setSubmittedAtBackend(verifiedTimestamp.getBackendTime());

        questionSessionRepository.save(session);
    }

    // 5. Generate Questions Session
    @Transactional
    public QuestionSessionResponse generateQuestions(UserPrincipal principal, String resumeIdStr, int numQuestions) {
        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        PracticeResumeTrack track = verifiedTrack.getResumeTrack();

        int sessionNumber = track.getTotalSessions() + 1;

        Map<String, Map<String, String>> previousSessions = new HashMap<>();
        if (sessionNumber != 1) {
            PreviousResumeSessionResponse prevResponse = resumeUtil.previousResumeSessionQuestions(track.getId(), null,
                    null);
            previousSessions = prevResponse.getSessionDict();
        }

        ResumeAiResponses.QuestionListResponse aiQuestions = resumeAiService.generateResumeQuestion(
                track.getSummary(), numQuestions, previousSessions);

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
                .resumeTrackId(track.getId())
                .time(durationMinutes)
                .questionBank(questionBank)
                .status("active")
                .timestamp(now)
                .build();

        questionSessionRepository.save(session);

        track.setTotalSessions(sessionNumber);
        practiceResumeTrackRepository.save(track);

        List<Map<String, Object>> formattedQuestionBank = questionBank.stream().map(q -> {
            Map<String, Object> map = new HashMap<>();
            map.put("questionId", q.getQuestionId());
            map.put("question", q.getQuestion());
            map.put("answer", q.getAnswer());
            map.put("feedback", q.getFeedback());
            map.put("score", q.getScore());
            return map;
        }).toList();

        return QuestionSessionResponse.builder()
                .sessionId(session.getId().toString())
                .sessionNumber(sessionNumber)
                .status("active")
                .time(durationMinutes)
                .timestamp(now)
                .questionBank(formattedQuestionBank)
                .build();
    }

    // 6. Save Single Answer
    @Transactional
    public void saveAnswer(UserPrincipal principal, String resumeIdStr, String sessionIdStr,
            SaveAnswerRequest request) {
        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        var verifiedSession = resumeVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedTrack.getResumeId());
        QuestionSession session = verifiedSession.getSessionDoc();

        resumeVerificationService.verifySessionStatus(session);
        resumeVerificationService.verifySessionTime(session, session.getId());
        resumeVerificationService.verifyQuestionId(session, request.getQuestionId());

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

    // 7. Reattempt Question Session
    @Transactional
    public Map<String, String> reattemptSession(UserPrincipal principal, String resumeIdStr, String sessionIdStr) {
        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        var verifiedSession = resumeVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedTrack.getResumeId());
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
                .resumeTrackId(verifiedTrack.getResumeId())
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
    public void fakeSubmitSession(UserPrincipal principal, String resumeIdStr, String sessionIdStr) {
        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        var verifiedSession = resumeVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedTrack.getResumeId());
        QuestionSession session = verifiedSession.getSessionDoc();

        resumeVerificationService.verifySessionStatus(session);

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
    public ResumeAiResponses.ResumeEvaluationResponse generateFeedback(UserPrincipal principal, String resumeIdStr,
            String sessionIdStr) {
        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        var verifiedSession = resumeVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedTrack.getResumeId());
        QuestionSession session = verifiedSession.getSessionDoc();

        resumeVerificationService.verifySessionStatus2(session);

        ResumeAiResponses.ResumeEvaluationResponse evaluation = resumeAiService.evaluateResumeAnswers(
                verifiedTrack.getResumeTrack().getSummary(), session.getQuestionBank());

        if (evaluation.getFeedbackPerQuestion() != null) {
            Map<Integer, ResumeAiResponses.QuestionFeedback> feedbackMap = new HashMap<>();
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

    // 10. Get All Sessions
    public Map<String, Object> getAllSessions(UserPrincipal principal, String resumeIdStr) {
        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        List<QuestionSession> sessions = questionSessionRepository
                .findByResumeTrackIdOrderByTimestampAsc(verifiedTrack.getResumeId());

        List<Map<String, Object>> sessionList = sessions.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("question_session_id", s.getId().toString());
            m.put("session_number", s.getSessionNumber());
            m.put("status", s.getStatus());
            m.put("timestamp", s.getTimestamp());
            return m;
        }).toList();

        return Map.of("resume_id", verifiedTrack.getResumeId().toString(), "sessions", sessionList);
    }

    // 11. Get Single Session Full Data
    public Map<String, Object> getSessionData(UserPrincipal principal, String resumeIdStr, String sessionIdStr) {
        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        var verifiedSession = resumeVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedTrack.getResumeId());

        return Map.of("resume_id", verifiedTrack.getResumeId().toString(), "session", verifiedSession.getSessionDoc());
    }

    // 12. Delete Entire Resume Track
    @Transactional
    public void deleteResume(UserPrincipal principal, String resumeIdStr) {
        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        questionSessionRepository.deleteByResumeTrackId(verifiedTrack.getResumeId());
        practiceResumeTrackRepository.deleteById(verifiedTrack.getResumeId());
    }

    // 13. Delete Single Question Session
    @Transactional
    public void deleteSession(UserPrincipal principal, String resumeIdStr, String sessionIdStr) {
        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        var verifiedSession = resumeVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedTrack.getResumeId());

        long sameSessionCount = questionSessionRepository.countByResumeTrackIdAndSessionNumber(
                verifiedTrack.getResumeId(), verifiedSession.getSessionDoc().getSessionNumber());

        if (sameSessionCount <= 1) {
            throw new CustomException("Can't delete complete session entirely, either reattempt or leave.",
                    HttpStatus.BAD_REQUEST);
        }

        questionSessionRepository.deleteById(verifiedSession.getSessionObjId());
    }

    // 14. Delete Answers & Reattempt
    @Transactional
    public void deleteAndReattempt(UserPrincipal principal, String resumeIdStr, String sessionIdStr) {
        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        var verifiedSession = resumeVerificationService.verifyQuestionSession(sessionIdStr,
                verifiedTrack.getResumeId());
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
    public Map<String, Object> combinedFeedbackLastXSessions(UserPrincipal principal, String resumeIdStr, int x) {
        if (x <= 1) {
            throw new CustomException("Invalid value of x", HttpStatus.BAD_REQUEST);
        }

        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        PracticeResumeTrack track = verifiedTrack.getResumeTrack();

        PreviousResumeSessionResponse sessionData = resumeUtil.previousResumeSessionQuestions(track.getId(), x, null);

        ResumeAiResponses.CombinedFeedbackDetails combinedDetails = resumeAiService
                .generateResumeCombinedDiffSessionFeedback(
                        track.getSummary(), new HashMap<>(sessionData.getSessionDict()));
        Map<String, Object> feedback = combinedDetails != null ? Map.of(
                "strengths", combinedDetails.getStrengths() != null ? combinedDetails.getStrengths() : "",
                "weaknesses", combinedDetails.getWeaknesses() != null ? combinedDetails.getWeaknesses() : "",
                "improvement", combinedDetails.getImprovement() != null ? combinedDetails.getImprovement() : "",
                "recommendations",
                combinedDetails.getRecommendations() != null ? combinedDetails.getRecommendations() : "") : Map.of();

        Map<String, Object> feedbackRecord = new HashMap<>();
        feedbackRecord.put("sessions_used", sessionData.getSessionsUsed());
        feedbackRecord.put("feedback", feedback);
        feedbackRecord.put("type", "different");
        feedbackRecord.put("timestamp", TimeUtil.generateTimestamp());

        if (track.getCombinedFeedback() == null) {
            track.setCombinedFeedback(new ArrayList<>());
        }
        track.getCombinedFeedback().add(feedbackRecord);
        practiceResumeTrackRepository.save(track);

        return Map.of("sessions_used", sessionData.getSessionsUsed(), "feedback", feedback);
    }

    // 16. Progress: Combined Feedback Within Same Session Reattempts
    @Transactional
    public Map<String, Object> combinedFeedbackSameSession(UserPrincipal principal, String resumeIdStr,
            String sessionIdStr, int x) {
        if (x <= 1) {
            throw new CustomException("Invalid value of x", HttpStatus.BAD_REQUEST);
        }

        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        PracticeResumeTrack track = verifiedTrack.getResumeTrack();
        var verifiedSession = resumeVerificationService.verifyQuestionSession(sessionIdStr, track.getId());

        PreviousResumeSessionResponse sessionData = resumeUtil.previousResumeSessionQuestions(
                track.getId(), x, verifiedSession.getSessionDoc().getSessionNumber());

        ResumeAiResponses.CombinedFeedbackDetails combinedDetails = resumeAiService
                .generateResumeCombinedSameSessionFeedback(
                        track.getSummary(), new HashMap<>(sessionData.getSessionDict()));
        Map<String, Object> feedback = combinedDetails != null ? Map.of(
                "strengths", combinedDetails.getStrengths() != null ? combinedDetails.getStrengths() : "",
                "weaknesses", combinedDetails.getWeaknesses() != null ? combinedDetails.getWeaknesses() : "",
                "improvement", combinedDetails.getImprovement() != null ? combinedDetails.getImprovement() : "",
                "recommendations",
                combinedDetails.getRecommendations() != null ? combinedDetails.getRecommendations() : "") : Map.of();

        Map<String, Object> feedbackRecord = new HashMap<>();
        feedbackRecord.put("sessions_used", sessionData.getSessionsUsed());
        feedbackRecord.put("feedback", feedback);
        feedbackRecord.put("type", "same");
        feedbackRecord.put("timestamp", TimeUtil.generateTimestamp());

        if (track.getCombinedFeedback() == null) {
            track.setCombinedFeedback(new ArrayList<>());
        }
        track.getCombinedFeedback().add(feedbackRecord);
        practiceResumeTrackRepository.save(track);

        return Map.of("sessions_used", sessionData.getSessionsUsed(), "feedback", feedback);
    }

    // 17. Get All 'Different Session' Progress History
    public Map<String, Object> getDifferentCombinedFeedback(UserPrincipal principal, String resumeIdStr) {
        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        PracticeResumeTrack track = verifiedTrack.getResumeTrack();

        List<Map<String, Object>> result = new ArrayList<>();
        if (track.getCombinedFeedback() != null) {
            for (var item : track.getCombinedFeedback()) {
                if ("different".equals(item.get("type"))) {
                    result.add(Map.of(
                            "sessions_used", item.getOrDefault("sessions_used", Map.of()),
                            "feedback", item.getOrDefault("feedback", ""),
                            "type", item.get("type"),
                            "timestamp", item.getOrDefault("timestamp", "")));
                }
            }
        }

        return Map.of("resume_id", track.getId().toString(), "combined_feedback", result);
    }

    // 18. Get All 'Same Session' Progress History
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSameCombinedFeedback(UserPrincipal principal, String resumeIdStr, int sessionNumber) {
        var verifiedTrack = resumeVerificationService.verifyResume(resumeIdStr, principal.getId());
        PracticeResumeTrack track = verifiedTrack.getResumeTrack();

        List<Map<String, Object>> result = new ArrayList<>();
        if (track.getCombinedFeedback() != null) {
            for (var item : track.getCombinedFeedback()) {
                if ("same".equals(item.get("type"))) {
                    Map<String, Object> sessionsUsed = (Map<String, Object>) item.get("sessions_used");
                    if (sessionsUsed != null && !sessionsUsed.isEmpty()) {
                        Object firstVal = sessionsUsed.values().iterator().next();
                        if (firstVal instanceof Map<?, ?> firstSessionMap) {
                            Object sNum = firstSessionMap.get("session_number");
                            if (sNum != null && Integer.parseInt(sNum.toString()) == sessionNumber) {
                                result.add(Map.of(
                                        "sessions_used", sessionsUsed,
                                        "feedback", item.getOrDefault("feedback", ""),
                                        "type", item.get("type"),
                                        "timestamp", item.getOrDefault("timestamp", "")));
                            }
                        }
                    }
                }
            }
        }

        return Map.of("resume_id", track.getId().toString(), "combined_feedback", result);
    }
}