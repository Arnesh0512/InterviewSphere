package com.arnesh.interviewsphere.ai;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.arnesh.interviewsphere.dto.response.ContestAiResponses.ContestEvaluationResponse;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.prompt.ContestPrompts;
import com.arnesh.interviewsphere.service.OpenAiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContestAiService {

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;






    public String generateSummary(String resumeText) {
        try {
            String content = "Resume Content:\n" + resumeText;
            String rawResponse = openAiService.callChatGpt(ContestPrompts.GENERATE_SUMMARY_PROMPT, content, 0.2, ContestPrompts.GENERATE_SUMMARY_RESPONSE);

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


    



    public ContestEvaluationResponse evaluateResumeScore(
            String resumeText,
            List<Map<String, Object>> resumeQuestions,
            String company,
            String role,
            List<String> skills) {
        try {
            String questionsText = resumeQuestions.stream()
                    .map(q -> q.get("question_id") + ". " + q.get("question"))
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

            String rawJson = openAiService.callChatGpt(ContestPrompts.EVALUATE_RESUME_SCORE_PROMPT, content, 0.2, ContestPrompts.EVALUATE_RESUME_SCORE_RESPONSE);
            return objectMapper.readValue(rawJson, ContestEvaluationResponse.class);

        } catch (JsonProcessingException | CustomException e) {
            throw new CustomException("Invalid JSON returned by AI for Resume evaluation",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    



    public ContestEvaluationResponse evaluateCodingScore(Object evaluationInput) {
        try {
            String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(evaluationInput);
            String rawJson = openAiService.callChatGpt(ContestPrompts.EVALUATE_CODING_SCORE_PROMPT, content, 0.2, ContestPrompts.EVALUATE_CODING_SCORE_RESPONSE);

            return objectMapper.readValue(rawJson, ContestEvaluationResponse.class);

        } catch (JsonProcessingException | CustomException e) {
            throw new CustomException("Invalid JSON returned by AI for Coding evaluation",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    



    public ContestEvaluationResponse evaluateConceptScore(Object evaluationInput) {
        try {
            String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(evaluationInput);
            String rawJson = openAiService.callChatGpt(ContestPrompts.EVALUATE_CONCEPT_SCORE_PROMPT, content, 0.2, ContestPrompts.EVALUATE_CONCEPT_SCORE_RESPONSE);

            return objectMapper.readValue(rawJson, ContestEvaluationResponse.class);

        } catch (JsonProcessingException | CustomException e) {
            throw new CustomException("Invalid JSON returned by AI for Concept evaluation",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    


    
    public ContestEvaluationResponse evaluateHrScore(Object evaluationInput, String resumeSummary) {
        try {
            Map<String, Object> payload = Map.of(
                    "resume_summary", resumeSummary,
                    "responses", evaluationInput);

            String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
            String rawJson = openAiService.callChatGpt(ContestPrompts.EVALUATE_HR_SCORE_PROMPT, content, 0.2, ContestPrompts.EVALUATE_HR_SCORE_RESPONSE);

            return objectMapper.readValue(rawJson, ContestEvaluationResponse.class);

        } catch (JsonProcessingException | CustomException e) {
            throw new CustomException("Invalid JSON returned by AI for HR evaluation",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
