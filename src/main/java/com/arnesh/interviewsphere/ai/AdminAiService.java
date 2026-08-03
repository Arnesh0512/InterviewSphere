package com.arnesh.interviewsphere.ai;

import com.arnesh.interviewsphere.dto.response.AdminAiResponses.QuestionListResponse;
import com.arnesh.interviewsphere.dto.response.AdminAiResponses.ValidationResponse;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.prompt.AdminPrompts;
import com.arnesh.interviewsphere.service.OpenAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAiService {

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    /**
     * Validates whether provided skills align with the specified role using
     * ChatGPT.
     */
    public Boolean validateRoleSkills(String role, List<String> skills) {
        String content = "Role: " + role + ", Skills: " + String.join(", ", skills);

        try {
            String rawJson = openAiService.callChatGpt(AdminPrompts.VALIDATE_ROLE_SKILLS, content, 0.4);
            ValidationResponse response = objectMapper.readValue(rawJson, ValidationResponse.class);

            if (response == null || response.getValid() == null) {
                throw new CustomException("valid must be boolean", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (!response.getValid()) {
                throw new CustomException("Role and skills mismatch", HttpStatus.BAD_REQUEST);
            }

            return true;

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException("Invalid JSON returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generates questions for resume evaluation.
     */
    public List<String> generateResumeQuestions(String company, String role, List<String> skills, int questionCount) {
        String content = String.format("""
                Company: %s
                Role: %s
                Skills: %s
                Number of questions: %d
                """, company, role, String.join(", ", skills), questionCount);

        try {
            String rawJson = openAiService.callChatGpt(AdminPrompts.GENERATE_RESUME_QUESTIONS, content, 0.7);
            QuestionListResponse response = objectMapper.readValue(rawJson, QuestionListResponse.class);

            if (response == null || response.getQuestions() == null) {
                throw new CustomException("questions must be list", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return response.getQuestions();

        } catch (Exception e) {
            throw new CustomException("Invalid JSON returned by AI for resume questions",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generates technical core conceptual interview questions.
     */
    public List<String> generateConceptQuestions(String role, List<String> skills, int questionCount) {
        String content = String.format("""
                Role: %s
                Skills: %s
                Number of questions: %d
                """, role, String.join(", ", skills), questionCount);

        try {
            String rawJson = openAiService.callChatGpt(AdminPrompts.GENERATE_CONCEPT_QUESTIONS, content, 0.7);
            QuestionListResponse response = objectMapper.readValue(rawJson, QuestionListResponse.class);

            if (response == null || response.getQuestions() == null) {
                throw new CustomException("questions must be list", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return response.getQuestions();

        } catch (Exception e) {
            throw new CustomException("Invalid JSON returned by AI for concept questions",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generates HR interview questions assessing soft skills and ethics.
     */
    public List<String> generateHrQuestions(String role, int questionCount) {
        String content = String.format("""
                Role: %s
                Number of questions: %d
                """, role, questionCount);

        try {
            String rawJson = openAiService.callChatGpt(AdminPrompts.GENERATE_HR_QUESTIONS, content, 0.7);
            QuestionListResponse response = objectMapper.readValue(rawJson, QuestionListResponse.class);

            if (response == null || response.getQuestions() == null) {
                throw new CustomException("questions must be list", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return response.getQuestions();

        } catch (Exception e) {
            throw new CustomException("Invalid JSON returned by AI for HR questions", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}