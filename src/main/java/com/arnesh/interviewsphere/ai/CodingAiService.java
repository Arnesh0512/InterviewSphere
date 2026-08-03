package com.arnesh.interviewsphere.ai;

import com.arnesh.interviewsphere.dto.response.CodingAiResponses.CodingEvaluationResponse;
import com.arnesh.interviewsphere.dto.response.CodingAiResponses.CombinedFeedbackDetails;
import com.arnesh.interviewsphere.dto.response.CodingAiResponses.CombinedFeedbackResponse;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.prompt.CodingPrompts;
import com.arnesh.interviewsphere.service.OpenAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CodingAiService {

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    /**
     * Evaluates coding question responses against LLM prompt guidelines.
     */
    public CodingEvaluationResponse evaluateCodingAnswers(List<?> questions) {
        try {
            String content = "Coding Questions and Answers:\n\n"
                    + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(questions);
            String rawJson = openAiService.callChatGpt(CodingPrompts.EVALUATE_CODING_ANSWERS, content, 0.2);

            CodingEvaluationResponse response = objectMapper.readValue(rawJson, CodingEvaluationResponse.class);

            if (response == null || response.getFeedbackPerQuestion() == null) {
                throw new CustomException("feedback_per_question must be list", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return response;

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException("Invalid JSON returned by AI for coding evaluation",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generates combined feedback across DIFFERENT coding sessions.
     */
    public CombinedFeedbackDetails generateCodingCombinedDiffSessionFeedback(Map<String, Object> sessionData) {
        try {
            String content = "Session wise Coding Attempts:\n"
                    + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sessionData);
            String rawJson = openAiService.callChatGpt(CodingPrompts.COMBINED_DIFF_SESSION_FEEDBACK, content, 0.2);

            CombinedFeedbackResponse result = objectMapper.readValue(rawJson, CombinedFeedbackResponse.class);

            if (result == null || result.getFeedback() == null) {
                throw new CustomException("Invalid JSON returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return result.getFeedback();

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException("Invalid JSON returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generates combined feedback across reattempts of the SAME coding session.
     */
    public CombinedFeedbackDetails generateCodingCombinedSameSessionFeedback(Map<String, Object> sessionData) {
        try {
            String content = "Session wise Coding Attempts:\n"
                    + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sessionData);
            String rawJson = openAiService.callChatGpt(CodingPrompts.COMBINED_SAME_SESSION_FEEDBACK, content, 0.2);

            CombinedFeedbackResponse result = objectMapper.readValue(rawJson, CombinedFeedbackResponse.class);

            if (result == null || result.getFeedback() == null) {
                throw new CustomException("Invalid JSON returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return result.getFeedback();

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException("Invalid JSON returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}