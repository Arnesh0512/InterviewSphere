package com.arnesh.interviewsphere.ai;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.arnesh.interviewsphere.dto.response.AdminAiResponses.QuestionListResponse;
import com.arnesh.interviewsphere.dto.response.AdminAiResponses.ValidationResponse;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.prompt.AdminPrompts;
import com.arnesh.interviewsphere.service.OpenAiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminAiService {

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    public Boolean validateRoleSkills(String role, List<String> skills) {
        String content = "Role: " + role + ", Skills: " + String.join(", ", skills);

        try {
            String rawJson = openAiService.callChatGpt(AdminPrompts.VALIDATE_ROLE_SKILLS_PROMPT, content, 0.4, AdminPrompts.VALIDATE_ROLE_SKILLS_RESPONSE);
            ValidationResponse response = objectMapper.readValue(rawJson, ValidationResponse.class);

            if (response == null || response.getValid() == null) {
                throw new CustomException("valid must be boolean", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (!response.getValid()) {
                throw new CustomException("Role and skills mismatch", HttpStatus.BAD_REQUEST);
            }

            return true;

        } catch (JsonProcessingException | CustomException e) {
            throw new CustomException("Invalid JSON returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<String> generateResumeQuestions(String company, String role, List<String> skills, int questionCount) {
        String content = String.format("""
                Company: %s
                Role: %s
                Skills: %s
                Number of questions: %d
                """, company, role, String.join(", ", skills), questionCount);

        try {
            String rawJson = openAiService.callChatGpt(AdminPrompts.GENERATE_RESUME_QUESTIONS_PROMPT, content, 0.7, AdminPrompts.GENERATE_RESUME_QUESTIONS_RESPONSE);
            QuestionListResponse response = objectMapper.readValue(rawJson, QuestionListResponse.class);

            if (response == null || response.getQuestions() == null) {
                throw new CustomException("questions must be list", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return response.getQuestions();

        } catch (JsonProcessingException | CustomException e) {
            throw new CustomException("Invalid JSON returned by AI for resume questions",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<String> generateConceptQuestions(String role, List<String> skills, int questionCount) {
        String content = String.format("""
                Role: %s
                Skills: %s
                Number of questions: %d
                """, role, String.join(", ", skills), questionCount);

        try {
            String rawJson = openAiService.callChatGpt(AdminPrompts.GENERATE_CONCEPT_QUESTIONS_PROMPT, content, 0.7, AdminPrompts.GENERATE_CONCEPT_QUESTIONS_RESPONSE);
            QuestionListResponse response = objectMapper.readValue(rawJson, QuestionListResponse.class);

            if (response == null || response.getQuestions() == null) {
                throw new CustomException("questions must be list", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return response.getQuestions();

        } catch (JsonProcessingException | CustomException e) {
            throw new CustomException("Invalid JSON returned by AI for concept questions",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<String> generateHrQuestions(String role, int questionCount) {
        String content = String.format("""
                Role: %s
                Number of questions: %d
                """, role, questionCount);

        try {
            String rawJson = openAiService.callChatGpt(AdminPrompts.GENERATE_HR_QUESTIONS_PROMPT, content, 0.7, AdminPrompts.GENERATE_HR_QUESTIONS_RESPONSE);
            QuestionListResponse response = objectMapper.readValue(rawJson, QuestionListResponse.class);

            if (response == null || response.getQuestions() == null) {
                throw new CustomException("questions must be list", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return response.getQuestions();

        } catch (JsonProcessingException | CustomException e) {
            throw new CustomException("Invalid JSON returned by AI for HR questions", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
