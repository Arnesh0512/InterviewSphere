package com.arnesh.interviewsphere.ai;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.arnesh.interviewsphere.dto.response.GithubAiResponses.CombinedFeedbackDetails;
import com.arnesh.interviewsphere.dto.response.GithubAiResponses.CombinedFeedbackResponse;
import com.arnesh.interviewsphere.dto.response.GithubAiResponses.GithubEvaluationResponse;
import com.arnesh.interviewsphere.dto.response.GithubAiResponses.ProcessRepoResponse;
import com.arnesh.interviewsphere.dto.response.GithubAiResponses.QuestionListResponse;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.prompt.GithubPrompts;
import com.arnesh.interviewsphere.service.OpenAiService;
import com.arnesh.interviewsphere.util.GithubUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GithubAiService {

    private final OpenAiService openAiService;
    private final GithubUtil githubUtil;
    private final ObjectMapper objectMapper;

    /**
     * Fetches details and summarizes a GitHub repository.
     */
    public ProcessRepoResponse processRepo(String selectedRepoLink) {
        var details = githubUtil.fetchRepoDetails(selectedRepoLink);

        String content = String.format("""
                Repository Name: %s

                Description:
                %s

                Languages Used:
                %s

                README:
                %s
                """, details.getRepoName(), details.getDescription(), details.getLanguages(), details.getReadme());

        try {
            String summaryText = openAiService.callChatGpt(GithubPrompts.PROCESS_REPO_PROMPT, content, 0.2, GithubPrompts.PROCESS_REPO_RESPONSE);

            if (summaryText == null || summaryText.isBlank()) {
                throw new CustomException("Empty summary returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return ProcessRepoResponse.builder()
                    .repoName(details.getRepoName())
                    .summary(summaryText.trim())
                    .build();

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException("Failed to process repo summary: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generates interview questions specific to the repository and candidate
     * session history.
     */
    public QuestionListResponse generateGithubQuestion(
            String repoSummary,
            int numQuestions,
            Object previousSessions) {
        try {
            String prompt = GithubPrompts.getGenerateGithubQuestionPrompt(numQuestions);
            String content = String.format("""
                    Repository Summary:
                    %s

                    Previous Sessions:
                    %s
                    """, repoSummary,
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(previousSessions));

            String rawJson = openAiService.callChatGpt(prompt, content, 0.4, GithubPrompts.GENERATE_GITHUB_QUESTION_RESPONSE);
            QuestionListResponse response = objectMapper.readValue(rawJson, QuestionListResponse.class);

            if (response == null || response.getQuestions() == null) {
                throw new CustomException("Questions must be a list", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (response.getQuestions().size() != numQuestions) {
                throw new CustomException("Incorrect number of questions returned", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return response;

        } catch (JsonProcessingException | CustomException e) {
            throw new CustomException("Invalid JSON returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Evaluates answers provided for GitHub repository questions.
     */
    public GithubEvaluationResponse evaluateGithubAnswers(String repoSummary, List<?> questionBank) {
        try {
            String content = String.format("""
                    Repository Summary:
                    %s

                    Question & Answers:
                    %s
                    """, repoSummary, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(questionBank));

            String rawJson = openAiService.callChatGpt(GithubPrompts.EVALUATE_GITHUB_ANSWERS_PROMPT, content, 0.2, GithubPrompts.EVALUATE_GITHUB_ANSWERS_RESPONSE);
            GithubEvaluationResponse response = objectMapper.readValue(rawJson, GithubEvaluationResponse.class);

            if (response == null || response.getFeedbackPerQuestion() == null) {
                throw new CustomException("feedback_per_question must be list", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return response;

        } catch (JsonProcessingException | CustomException e) {
            throw new CustomException("Invalid JSON returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generates combined feedback across DIFFERENT GitHub interview sessions.
     */
    public CombinedFeedbackDetails generateGithubCombinedDiffSessionFeedback(
            String repoSummary,
            Map<String, Object> sessionData) {
        try {
            String content = String.format("""
                    Repository Summary:
                    %s

                    Session wise Question & Answers:
                    %s
                    """, repoSummary, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sessionData));

            String rawJson = openAiService.callChatGpt(GithubPrompts.COMBINED_DIFF_SESSION_FEEDBACK_PROMPT, content, 0.2, GithubPrompts.COMBINED_DIFF_SESSION_FEEDBACK_RESPONSE);
            CombinedFeedbackResponse result = objectMapper.readValue(rawJson, CombinedFeedbackResponse.class);

            if (result == null || result.getFeedback() == null) {
                throw new CustomException("Invalid JSON returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return result.getFeedback();

        } catch (JsonProcessingException | CustomException e) {
            throw new CustomException("Invalid JSON returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generates combined feedback across reattempts of the SAME GitHub
     * interview session.
     */
    public CombinedFeedbackDetails generateGithubCombinedSameSessionFeedback(
            String repoSummary,
            Map<String, Object> sessionData) {
        try {
            String content = String.format("""
                    Repository Summary:
                    %s

                    Session wise Question & Answers:
                    %s
                    """, repoSummary, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sessionData));

            String rawJson = openAiService.callChatGpt(GithubPrompts.COMBINED_SAME_SESSION_FEEDBACK_PROMPT, content, 0.2, GithubPrompts.COMBINED_SAME_SESSION_FEEDBACK_RESPONSE);
            CombinedFeedbackResponse result = objectMapper.readValue(rawJson, CombinedFeedbackResponse.class);

            if (result == null || result.getFeedback() == null) {
                throw new CustomException("Invalid JSON returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return result.getFeedback();

        } catch (JsonProcessingException | CustomException e) {
            throw new CustomException("Invalid JSON returned by AI", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
