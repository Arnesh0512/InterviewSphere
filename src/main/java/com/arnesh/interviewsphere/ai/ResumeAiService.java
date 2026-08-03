package com.arnesh.interviewsphere.ai;

import com.arnesh.interviewsphere.dto.response.ResumeAiResponses.*;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.prompt.ResumePrompts;
import com.arnesh.interviewsphere.service.OpenAiService;
import com.arnesh.interviewsphere.util.ResumeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResumeAiService {

    private final OpenAiService openAiService;
    private final ResumeUtil resumeUtil;
    private final ObjectMapper objectMapper;

    /**
     * Overloaded helper accepting MultipartFile directly from REST endpoints.
     */
    public String processResume(MultipartFile file, String ocrMode) {
        File tempFile = resumeUtil.convertMultipartToTempFile(file);
        try {
            return processResume(tempFile.getAbsolutePath(), ocrMode);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

/**
     * Extracts text from PDF and analyzes resume content.
     */
    public String processResume(String pdfPath, String ocrMode) {
        String extractedText = "Y".equalsIgnoreCase(ocrMode)
                ? resumeUtil.extractTextWithOcr(pdfPath)
                : resumeUtil.extractTextWithoutOcr(pdfPath);

        if (extractedText == null || extractedText.isBlank()) {
            throw new CustomException("No text extracted from PDF.", HttpStatus.BAD_REQUEST);
        }

        try {
            String content = "Resume Content:\n" + extractedText;
            String summaryText = openAiService.callChatGpt(ResumePrompts.PROCESS_RESUME, content, 0.2);

            if (summaryText == null || summaryText.isBlank()) {
                throw new CustomException("Empty summary returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return summaryText.trim();

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException("Failed to analyze resume text: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generates interview questions based on candidate resume summary and session
     * history.
     */
    public QuestionListResponse generateResumeQuestion(
            String summaryText,
            int numQuestions,
            Object previousSessions) {
        try {
            String prompt = ResumePrompts.getGenerateResumeQuestionPrompt(numQuestions);
            String content = String.format("""
                    Resume Summary:
                    %s

                    Previous Sessions:
                    %s
                    """, summaryText,
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(previousSessions));

            String rawJson = openAiService.callChatGpt(prompt, content, 0.4);
            QuestionListResponse response = objectMapper.readValue(rawJson, QuestionListResponse.class);

            if (response == null || response.getQuestions() == null) {
                throw new CustomException("Questions must be a list", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (response.getQuestions().size() != numQuestions) {
                throw new CustomException("Incorrect number of questions returned", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return response;

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException("Invalid JSON returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Evaluates candidate responses to resume questions.
     */
    public ResumeEvaluationResponse evaluateResumeAnswers(String summaryText, List<?> questionBank) {
        try {
            String content = String.format("""
                    Resume Summary:
                    %s

                    Question & Answers:
                    %s
                    """, summaryText, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(questionBank));

            String rawJson = openAiService.callChatGpt(ResumePrompts.EVALUATE_RESUME_ANSWERS, content, 0.2);
            ResumeEvaluationResponse response = objectMapper.readValue(rawJson, ResumeEvaluationResponse.class);

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
     * Generates combined feedback across DIFFERENT resume interview sessions.
     */
    public CombinedFeedbackDetails generateResumeCombinedDiffSessionFeedback(
            String summaryText,
            Map<String, Object> sessionData) {
        try {
            String content = String.format("""
                    Resume Summary:
                    %s

                    Session wise Question & Answers:
                    %s
                    """, summaryText, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sessionData));

            String rawJson = openAiService.callChatGpt(ResumePrompts.COMBINED_DIFF_SESSION_FEEDBACK, content, 0.2);
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
     * Generates combined feedback across reattempts of the SAME resume interview
     * session.
     */
    public CombinedFeedbackDetails generateResumeCombinedSameSessionFeedback(
            String summaryText,
            Map<String, Object> sessionData) {
        try {
            String content = String.format("""
                    Resume Summary:
                    %s

                    Session wise Question & Answers:
                    %s
                    """, summaryText, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sessionData));

            String rawJson = openAiService.callChatGpt(ResumePrompts.COMBINED_SAME_SESSION_FEEDBACK, content, 0.2);
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