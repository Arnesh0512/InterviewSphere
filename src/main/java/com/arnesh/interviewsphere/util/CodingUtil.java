package com.arnesh.interviewsphere.util;

import com.arnesh.interviewsphere.dto.response.PreviousCodingSessionResponse;
import com.arnesh.interviewsphere.entity.LeetCodeQuestion;
import com.arnesh.interviewsphere.entity.QuestionSession;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.LeetCodeQuestionRepository;
import com.arnesh.interviewsphere.repository.QuestionSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class CodingUtil {

    private final QuestionSessionRepository questionSessionRepository;
    private final LeetCodeQuestionRepository leetCodeQuestionRepository;
    private final ThreadPoolTaskScheduler taskScheduler;

    /**
     * Gets all unique question IDs used in previous sessions for a coding track.
     */
    public List<String> getUsedCodingQuestionIds(UUID codingId) {
        List<QuestionSession> sessions = questionSessionRepository.findByCodingTrackId(codingId);
        Set<String> usedQuestions = new HashSet<>();

        for (QuestionSession session : sessions) {
            if (session.getQuestionBank() != null) {
                for (QuestionSession.Question q : session.getQuestionBank()) {
                    if (q.getQuestionId() != null) {
                        usedQuestions.add(q.getQuestionId().toString());
                    }
                }
            }
        }
        return new ArrayList<>(usedQuestions);
    }

    /**
     * Retrieves previous coding session questions and formats them for feedback generation.
     */
    public PreviousCodingSessionResponse previousCodingSessionQuestions(UUID codingId, Integer x, Integer sessionNumber) {
        List<QuestionSession> sessions;

        if (sessionNumber != null) {
            sessions = questionSessionRepository.findByCodingTrackIdAndStatusAndSessionNumberOrderByTimestampDesc(
                    codingId, "passive", sessionNumber);
        } else {
            sessions = questionSessionRepository.findByCodingTrackIdAndStatusOrderByTimestampDesc(codingId, "passive");
        }

        if (sessionNumber == null) {
            Map<Integer, QuestionSession> latestSessionsMap = new LinkedHashMap<>();
            for (QuestionSession s : sessions) {
                latestSessionsMap.putIfAbsent(s.getSessionNumber(), s);
            }
            sessions = new ArrayList<>(latestSessionsMap.values());
        }

        if (x != null) {
            if (sessions.size() > x) {
                sessions = sessions.subList(0, x);
            }
            if (sessions.size() <= 1) {
                throw new CustomException("Need at least 2 sessions for combined feedback.", HttpStatus.BAD_REQUEST);
            }
        }

        Set<String> questionIds = new HashSet<>();
        List<QuestionSession> sessionsDiff = (sessionNumber != null && !sessions.isEmpty())
                ? List.of(sessions.get(0))
                : sessions;

        for (QuestionSession s : sessionsDiff) {
            if (s.getQuestionBank() != null) {
                for (QuestionSession.Question q : s.getQuestionBank()) {
                    if (q.getAnswer() != null && !q.getAnswer().isBlank() && q.getQuestionId() != null) {
                        questionIds.add(q.getQuestionId().toString());
                    }
                }
            }
        }

        List<LeetCodeQuestion> problems = leetCodeQuestionRepository.findAllById(questionIds);
        Map<String, String> problemMap = new HashMap<>();
        for (LeetCodeQuestion p : problems) {
            problemMap.put(p.getQuestionId(), p.getProblemDescription());
        }

        Map<String, Map<String, String>> sessionDict = new LinkedHashMap<>();
        Map<String, Map<String, Object>> sessionsUsed = new LinkedHashMap<>();

        for (int idx = 0; idx < sessions.size(); idx++) {
            QuestionSession s = sessions.get(idx);
            Map<String, String> formattedQuestions = new LinkedHashMap<>();

            if (s.getQuestionBank() != null) {
                for (QuestionSession.Question q : s.getQuestionBank()) {
                    UUID qid = q.getQuestionId();
                    String qidStr = qid != null ? qid.toString() : "";
                    String answer = q.getAnswer();
                    String language = q.getLanguage();

                    if (answer == null || answer.isBlank() || !problemMap.containsKey(qidStr)) {
                        continue;
                    }

                    String problemDescription = problemMap.get(qidStr);
                    formattedQuestions.put(problemDescription, "Language: " + language + "\n Answer:\n" + answer);
                }
            }

            sessionDict.put("session_" + (idx + 1), formattedQuestions);

            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("session_number", s.getSessionNumber());
            sessionInfo.put("session_id", s.getId().toString());
            sessionsUsed.put(s.getTimestamp().toString(), sessionInfo);
        }

        return PreviousCodingSessionResponse.builder()
                .sessionDict(sessionDict)
                .sessionsUsed(sessionsUsed)
                .build();
    }

    /**
     * Schedules an auto-submit callback task when the session time expires.
     */
    public void autoSubmit(
            UUID codingId,
            UUID questionSessionId,
            String token,
            OffsetDateTime startTime,
            int durationInMinutes,
            Consumer<AutoSubmitContext> submitCallback) {
        OffsetDateTime endTime = startTime.plusMinutes(durationInMinutes).plusMinutes(1);
        OffsetDateTime now = OffsetDateTime.now();

        long waitSeconds = Duration.between(now, endTime).getSeconds();

        Runnable task = () -> submitCallback.accept(
                new AutoSubmitContext(codingId, questionSessionId, OffsetDateTime.now(), token));

        if (waitSeconds > 0) {
            taskScheduler.schedule(task, endTime.toInstant());
        } else {
            task.run();
        }
    }

    public record AutoSubmitContext(
            UUID codingId,
            UUID questionSessionId,
            OffsetDateTime submitTime,
            String token) {
    }
}