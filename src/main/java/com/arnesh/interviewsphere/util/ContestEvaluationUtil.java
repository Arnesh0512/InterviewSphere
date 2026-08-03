package com.arnesh.interviewsphere.util;

import com.arnesh.interviewsphere.ai.ContestAiService;
import com.arnesh.interviewsphere.dto.request.CodingEvaluationInput;
import com.arnesh.interviewsphere.dto.request.ConceptEvaluationInput;
import com.arnesh.interviewsphere.dto.request.HrEvaluationInput;
import com.arnesh.interviewsphere.dto.response.ContestAiResponses;
import com.arnesh.interviewsphere.entity.ContestCandidateResponse;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.ContestCandidateResponseRepository;
import com.arnesh.interviewsphere.repository.ContestRepository;
import com.arnesh.interviewsphere.repository.LeetCodeQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContestEvaluationUtil {

    private final ContestCandidateResponseRepository contestCandidateResponseRepository;
    private final ContestRepository contestRepository;
    private final LeetCodeQuestionRepository leetCodeQuestionRepository;
    private final ContestAiService contestAiService;
    private final ThreadPoolTaskScheduler taskScheduler;

    /**
     * Schedules an auto-submit task to run when the contest end time arrives.
     */
    public void autoSubmit(
            UUID contestId,
            String token,
            OffsetDateTime endTime,
            Consumer<AutoSubmitContext> submitCallback) {
        OffsetDateTime now = OffsetDateTime.now();
        long waitSeconds = Duration.between(now, endTime).getSeconds();

        Runnable task = () -> submitCallback.accept(new AutoSubmitContext(contestId, endTime, token));

        if (waitSeconds > 0) {
            taskScheduler.schedule(task, endTime.toInstant());
        } else {
            task.run();
        }
    }

    /**
     * Evaluates candidate coding submissions, assigns AI scores & feedback, and
     * updates DB.
     */
    @SuppressWarnings("unchecked")
    public void generateCodingScores(UUID contestId, UUID candidateId, ContestCandidateResponse contestCandidate) {
        Map<String, Object> codingData = contestCandidate.getCoding();
        if (codingData == null || !codingData.containsKey("questionBank")) {
            return;
        }

        List<Map<String, Object>> questionBank = (List<Map<String, Object>>) codingData.get("questionBank");

        List<Integer> questionIds = questionBank.stream()
                .map(q -> {
                    Object id = q.get("questionId");
                    return id != null ? Integer.parseInt(id.toString()) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Integer, String> questionMap = leetCodeQuestionRepository.findAllById(questionIds).stream()
                .collect(Collectors.toMap(q -> q.getQuestionId(), q -> q.getProblemDescription()));

        List<CodingEvaluationInput> evaluationInput = questionBank.stream().map(q -> {
            Object id = q.get("questionId");
            Integer qid = id != null ? Integer.parseInt(id.toString()) : 0;
            return CodingEvaluationInput.builder()
                    .questionId(qid)
                    .problemDescription(questionMap.getOrDefault(qid, ""))
                    .answer((String) q.getOrDefault("answer", ""))
                    .language((String) q.getOrDefault("language", ""))
                    .build();
        }).collect(Collectors.toList());

        ContestAiResponses.ContestEvaluationResponse response = contestAiService.evaluateCodingScore(evaluationInput);

        Map<Integer, Double> scoreMap = new HashMap<>();
        Map<Integer, String> feedbackMap = new HashMap<>();

        if (response.getResults() != null) {
            for (var res : response.getResults()) {
                scoreMap.put(res.getQuestionId(), res.getScore());
                feedbackMap.put(res.getQuestionId(), res.getFeedback());
            }
        }

        for (var q : questionBank) {
            String qidStr = (String) q.get("questionId");
            q.put("score", scoreMap.getOrDefault(qidStr, 0.0));
            q.put("feedback", feedbackMap.getOrDefault(qidStr, ""));
        }

        codingData.put("overallFeedback", response.getOverallFeedback());
        contestCandidateResponseRepository.save(contestCandidate);
    }

    /**
     * Evaluates candidate concept submissions, assigns AI scores & feedback, and
     * updates DB.
     */
    @SuppressWarnings("unchecked")
    public void generateConceptScores(UUID contestId, UUID candidateId, ContestCandidateResponse contestCandidate) {
        Map<String, Object> conceptData = contestCandidate.getConcept();
        if (conceptData == null || !conceptData.containsKey("questionBank")) {
            return;
        }

        if (!contestRepository.existsById(contestId)) {
            throw new CustomException("Contest not found", HttpStatus.NOT_FOUND);
        }

        List<Map<String, Object>> questionBank = (List<Map<String, Object>>) conceptData.get("questionBank");

        List<ConceptEvaluationInput> evaluationInput = new ArrayList<>();
        for (var q : questionBank) {
            Object qidObj = q.get("questionId");
            Integer qid = qidObj != null ? Integer.parseInt(qidObj.toString()) : 0;
            String questionText = (String) q.getOrDefault("question", "");

            evaluationInput.add(ConceptEvaluationInput.builder()
                    .questionId(qid)
                    .question(questionText)
                    .answer((String) q.getOrDefault("answer", ""))
                    .build());
        }

        ContestAiResponses.ContestEvaluationResponse response = contestAiService.evaluateConceptScore(evaluationInput);

        Map<Integer, Double> scoreMap = new HashMap<>();
        Map<Integer, String> feedbackMap = new HashMap<>();

        if (response.getResults() != null) {
            for (var res : response.getResults()) {
                scoreMap.put(res.getQuestionId(), res.getScore());
                feedbackMap.put(res.getQuestionId(), res.getFeedback());
            }
        }

        for (var q : questionBank) {
            String qidStr = String.valueOf(q.get("questionId"));
            q.put("score", scoreMap.getOrDefault(qidStr, 0.0));
            q.put("feedback", feedbackMap.getOrDefault(qidStr, ""));
        }

        conceptData.put("overallFeedback", response.getOverallFeedback());
        contestCandidateResponseRepository.save(contestCandidate);
    }

    /**
     * Evaluates candidate HR audio/transcript submissions, assigns AI scores &
     * feedback, and updates DB.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void generateHrScores(UUID contestId, UUID candidateId, ContestCandidateResponse contestCandidate) {
        Map<String, Object> hrData = contestCandidate.getHr();
        if (hrData == null || !hrData.containsKey("questionBank")) {
            return;
        }

        List<Map<String, Object>> questionBank = (List<Map<String, Object>>) hrData.get("questionBank");



        
        List<HrEvaluationInput> evaluationInput = new ArrayList<>();
        for (var q : questionBank) {
            Object qidObj = q.get("questionId");
            Integer qid = qidObj != null ? Integer.parseInt(qidObj.toString()) : 0;
            String questionText = (String) q.getOrDefault("question", "");
            String transcript = (String) q.getOrDefault("transcript", "");

            evaluationInput.add(HrEvaluationInput.builder()
                    .questionId(qid)
                    .question(questionText)
                    .transcript(transcript)
                    .segmentedData((List) q.getOrDefault("segmentedData", List.of()))
                    .build());
        }

        Map<String, Object> resumeData = contestCandidate.getResume();
        String resumeSummary = resumeData != null ? (String) resumeData.getOrDefault("summary", "") : "";

        ContestAiResponses.ContestEvaluationResponse response = contestAiService.evaluateHrScore(evaluationInput,
                resumeSummary);

        Map<Integer, Double> scoreMap = new HashMap<>();
        Map<Integer, String> feedbackMap = new HashMap<>();

        if (response.getResults() != null) {
            for (var res : response.getResults()) {
                scoreMap.put(res.getQuestionId(), res.getScore());
                feedbackMap.put(res.getQuestionId(), res.getFeedback());
            }
        }

        for (var q : questionBank) {
            String qidStr = String.valueOf(q.get("questionId"));
            q.put("score", scoreMap.getOrDefault(qidStr, 0.0));
            q.put("feedback", feedbackMap.getOrDefault(qidStr, ""));
        }

        hrData.put("overallFeedback", response.getOverallFeedback());
        contestCandidateResponseRepository.save(contestCandidate);
    }

    public record AutoSubmitContext(
            UUID contestId,
            OffsetDateTime endTime,
            String token) {
    }
}