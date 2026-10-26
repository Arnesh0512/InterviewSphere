package com.arnesh.interviewsphere.util;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import com.arnesh.interviewsphere.dto.response.PreviousConceptSessionResponse;
import com.arnesh.interviewsphere.entity.QuestionSession;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.QuestionSessionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConceptUtil {

    private final QuestionSessionRepository questionSessionRepository;
    private final ThreadPoolTaskScheduler taskScheduler;

    
    public PreviousConceptSessionResponse previousConceptSessionQuestions(UUID conceptId, Integer x,
            Integer sessionNumber) {
        List<QuestionSession> allSessions;

        if (sessionNumber != null) {
            allSessions = questionSessionRepository.findByConceptTrackIdAndStatusAndSessionNumberOrderByTimestampDesc(
                    conceptId, "passive", sessionNumber);
        } else {
            allSessions = questionSessionRepository.findByConceptTrackIdAndStatusOrderByTimestampDesc(
                    conceptId, "passive");
        }

        if (sessionNumber == null) {
            Map<Integer, QuestionSession> latestSessionsMap = new LinkedHashMap<>();
            for (QuestionSession session : allSessions) {
                latestSessionsMap.putIfAbsent(session.getSessionNumber(), session);
            }
            allSessions = new ArrayList<>(latestSessionsMap.values());
        }

        allSessions.sort(Comparator.comparing((QuestionSession s) -> s.getTimestamp()).reversed());

        if (x != null) {
            if (allSessions.size() > x) {
                allSessions = allSessions.subList(0, x);
            }
            if (allSessions.size() <= 1) {
                throw new CustomException("Need at least 2 sessions for combined feedback.", HttpStatus.BAD_REQUEST);
            }
        }

        Map<String, Map<String, String>> sessionDict = new LinkedHashMap<>();
        Map<String, Map<String, Object>> sessionsUsed = new LinkedHashMap<>();

        for (int idx = 0; idx < allSessions.size(); idx++) {
            QuestionSession s = allSessions.get(idx);
            Map<String, String> questionAnswerMap = new LinkedHashMap<>();

            if (s.getQuestionBank() != null) {
                for (QuestionSession.Question q : s.getQuestionBank()) {
                        questionAnswerMap.put(q.getQuestion(), q.getAnswer());
                }
            }

            sessionDict.put("session_" + (idx + 1), questionAnswerMap);

            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("session_number", s.getSessionNumber());
            sessionInfo.put("session_id", s.getId().toString());
            sessionsUsed.put(s.getTimestamp().toString(), sessionInfo);
        }

        return PreviousConceptSessionResponse.builder()
                .sessionDict(sessionDict)
                .sessionsUsed(sessionsUsed)
                .build();
    }

    
    public void autoSubmit(
            UUID conceptId,
            UUID questionSessionId,
            String token,
            OffsetDateTime startTime,
            int durationInMinutes,
            Consumer<AutoSubmitContext> submitCallback) {
        OffsetDateTime endTime = startTime.plusMinutes(durationInMinutes).plusMinutes(1);
        OffsetDateTime now = OffsetDateTime.now();

        long waitSeconds = Duration.between(now, endTime).getSeconds();

        Runnable task = () -> submitCallback.accept(
                new AutoSubmitContext(conceptId, questionSessionId, OffsetDateTime.now(), token));

        if (waitSeconds > 0) {
            taskScheduler.schedule(task, endTime.toInstant());
        } else {
            task.run();
        }
    }

    public record AutoSubmitContext(
            UUID conceptId,
            UUID questionSessionId,
            OffsetDateTime submitTime,
            String token) {
    }
}