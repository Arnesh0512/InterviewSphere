package com.arnesh.interviewsphere.ai;

import com.arnesh.interviewsphere.dto.response.ConceptAiResponses.ConceptEvaluationResponse;
import com.arnesh.interviewsphere.dto.response.ConceptAiResponses.CombinedFeedbackDetails;
import com.arnesh.interviewsphere.dto.response.ConceptAiResponses.CombinedFeedbackResponse;
import com.arnesh.interviewsphere.dto.response.ConceptAiResponses.QuestionListResponse;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.prompt.ConceptPrompts;
import com.arnesh.interviewsphere.service.OpenAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConceptAiService {

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    /**
     * Generates deep CS concept questions based on topics and previous session
     * history[cite: 14].
     */
    public QuestionListResponse generateConceptTopicQuestions(
            List<String> topics,
            int numQuestions,
            Object previousSessions) {
        try {
            String prompt = ConceptPrompts.getGenerateConceptTopicQuestionsPrompt(numQuestions);
            String content = String.format("""
                    CS Topics:
                    %s

                    Previous Sessions:
                    %s
                    """, String.join(", ", topics),
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(previousSessions));

            String rawJson = openAiService.callChatGpt(prompt, content, 0.4);
            QuestionListResponse response = objectMapper.readValue(rawJson, QuestionListResponse.class);

            if (response == null || response.getQuestions() == null) {
                throw new CustomException("Questions must be a list", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return response;

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException("Invalid JSON returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Evaluates answers to computer science concept questions[cite: 14].
     */
    public ConceptEvaluationResponse evaluateConceptTopicAnswers(
            List<String> topics,
            List<?> questionBank) {
        try {
            String content = String.format("""
                    Topics Covered:
                    %s

                    Question & Answers:
                    %s
                    """, String.join(", ", topics),
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(questionBank));

            String rawJson = openAiService.callChatGpt(ConceptPrompts.EVALUATE_CONCEPT_TOPIC_ANSWERS, content, 0.2);
            ConceptEvaluationResponse response = objectMapper.readValue(rawJson, ConceptEvaluationResponse.class);

            if (response == null || response.getFeedbackPerQuestion() == null) {
                throw new CustomException("feedback_per_question must be list", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return response;

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException("Invalid JSON returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generates combined feedback across DIFFERENT conceptual interview
     * sessions[cite: 14].
     */
    public CombinedFeedbackDetails generateConceptCombinedDiffSessionFeedback(
            List<String> topics,
            Map<String, Object> sessionData) {
        try {
            String content = String.format("""
                    Selected Topics:
                    %s

                    Session wise Question & Answers:
                    %s
                    """, topics.toString(),
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sessionData));

            String rawJson = openAiService.callChatGpt(ConceptPrompts.COMBINED_DIFF_SESSION_FEEDBACK, content, 0.2);
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
     * Generates combined feedback across reattempts of the SAME conceptual
     * interview session[cite: 14].
     */
    public CombinedFeedbackDetails generateConceptCombinedSameSessionFeedback(
            List<String> topics,
            Map<String, Object> sessionData) {
        try {
            String content = String.format("""
                    Selected Topics:
                    %s

                    Session wise Question & Answers:
                    %s
                    """, topics.toString(),
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sessionData));

            String rawJson = openAiService.callChatGpt(ConceptPrompts.COMBINED_SAME_SESSION_FEEDBACK, content, 0.2);
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