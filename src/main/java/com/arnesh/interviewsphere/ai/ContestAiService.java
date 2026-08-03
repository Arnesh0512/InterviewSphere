package com.arnesh.interviewsphere.ai;

import com.arnesh.interviewsphere.dto.response.ContestAiResponses.ContestEvaluationResponse;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.prompt.ContestPrompts;
import com.arnesh.interviewsphere.service.OpenAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContestAiService {

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

/**
     * Generates a technical summary string from resume text.
     */
    public String generateSummary(String resumeText) {
        try {
            String content = "Resume Content:\n" + resumeText;
            // Note: Since ContestPrompts.GENERATE_SUMMARY returns a plain string, 
            // we call callChatGpt or directly return the output text without JSON mapping.
            String rawResponse = openAiService.callChatGpt(ContestPrompts.GENERATE_SUMMARY, content, 0.2);

            if (rawResponse == null || rawResponse.isBlank()) {
                throw new CustomException("Empty response returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return rawResponse.trim();

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException("Failed to generate summary: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Evaluates candidate resume evidence against a set of interview
     * questions[cite: 15].
     */
    public ContestEvaluationResponse evaluateResumeScore(
            String resumeText,
            Map<String, String> resumeQuestions,
            String company,
            String role,
            List<String> skills) {
        try {
            String questionsText = resumeQuestions.entrySet().stream()
                    .map(e -> e.getKey() + ". " + e.getValue())
                    .collect(Collectors.joining("\n"));

            String content = String.format("""
                    Resume:
                    %s

                    Questions:
                    %s

                    Company: %s
                    Role: %s
                    Skills: %s
                    """, resumeText, questionsText, company, role, String.join(", ", skills));

            String rawJson = openAiService.callChatGpt(ContestPrompts.EVALUATE_RESUME_SCORE, content, 0.2);
            return objectMapper.readValue(rawJson, ContestEvaluationResponse.class);

        } catch (Exception e) {
            throw new CustomException("Invalid JSON returned by AI for Resume evaluation",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Evaluates coding solutions in a contest[cite: 15].
     */
    public ContestEvaluationResponse evaluateCodingScore(Object evaluationInput) {
        try {
            String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(evaluationInput);
            String rawJson = openAiService.callChatGpt(ContestPrompts.EVALUATE_CODING_SCORE, content, 0.2);

            return objectMapper.readValue(rawJson, ContestEvaluationResponse.class);

        } catch (Exception e) {
            throw new CustomException("Invalid JSON returned by AI for Coding evaluation",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Evaluates conceptual answers in a contest[cite: 15].
     */
    public ContestEvaluationResponse evaluateConceptScore(Object evaluationInput) {
        try {
            String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(evaluationInput);
            String rawJson = openAiService.callChatGpt(ContestPrompts.EVALUATE_CONCEPT_SCORE, content, 0.2);

            return objectMapper.readValue(rawJson, ContestEvaluationResponse.class);

        } catch (Exception e) {
            throw new CustomException("Invalid JSON returned by AI for Concept evaluation",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Evaluates HR transcript answers against candidate resume summary[cite: 15].
     */
    public ContestEvaluationResponse evaluateHrScore(Object evaluationInput, String resumeSummary) {
        try {
            Map<String, Object> payload = Map.of(
                    "resume_summary", resumeSummary,
                    "responses", evaluationInput);

            String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
            String rawJson = openAiService.callChatGpt(ContestPrompts.EVALUATE_HR_SCORE, content, 0.2);

            return objectMapper.readValue(rawJson, ContestEvaluationResponse.class);

        } catch (Exception e) {
            throw new CustomException("Invalid JSON returned by AI for HR evaluation",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}