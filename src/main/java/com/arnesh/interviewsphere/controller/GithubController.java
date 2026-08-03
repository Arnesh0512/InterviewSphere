package com.arnesh.interviewsphere.controller;

import com.arnesh.interviewsphere.dto.request.SaveAnswerRequest;
import com.arnesh.interviewsphere.dto.request.SubmitSessionRequest;
import com.arnesh.interviewsphere.dto.response.ApiResponse;
import com.arnesh.interviewsphere.dto.response.GithubAiResponses;
import com.arnesh.interviewsphere.dto.response.GithubRepositoryResponse;
import com.arnesh.interviewsphere.dto.response.QuestionSessionResponse;
import com.arnesh.interviewsphere.security.UserPrincipal;
import com.arnesh.interviewsphere.service.GithubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GithubController {

    private final GithubService githubService;

    // 1. Fetch Repositories
    @PostMapping("/repos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRepositories(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_link") String githubLink) {
        List<GithubRepositoryResponse> repos = githubService.getRepositories(principal, githubLink);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "github_link", githubLink,
                "repositories", repos)));
    }

    // 2. Fetch Repository Details
    @PostMapping("/repo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRepositoryDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_link") String githubLink,
            @RequestParam("repo_link") String repoLink) {
        Map<String, Object> response = githubService.getRepositoryDetails(principal, githubLink, repoLink);
        return ResponseEntity.ok(ApiResponse.success("Repo details processed successfully", response));
    }

    // 3. Get All GitHub Tracks
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllGithubs(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Map<String, Object>> githubs = githubService.getAllGithubs(principal);
        return ResponseEntity.ok(ApiResponse.success(githubs));
    }

    // 4. Submit Session
    @PostMapping("/questions/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_id") String githubId,
            @RequestParam("question_session_id") String sessionId,
            @Valid @RequestBody SubmitSessionRequest request) {
        githubService.submitSession(principal, githubId, sessionId, request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 5. Generate New Questions Session
    @PostMapping("/questions/new")
    public ResponseEntity<ApiResponse<QuestionSessionResponse>> generateGithubQuestions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_id") String githubId,
            @RequestParam(value = "num_questions", defaultValue = "3") int numQuestions) {
        QuestionSessionResponse response = githubService.generateQuestions(principal, githubId, numQuestions);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 6. Save Single Answer
    @PostMapping("/questions/save")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveAnswer(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_id") String githubId,
            @RequestParam("question_session_id") String sessionId,
            @Valid @RequestBody SaveAnswerRequest request) {
        githubService.saveAnswer(principal, githubId, sessionId, request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 7. Reattempt Question Session
    @PutMapping("/questions/reattempt")
    public ResponseEntity<ApiResponse<Map<String, String>>> reattemptSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_id") String githubId,
            @RequestParam("question_session_id") String sessionId) {
        Map<String, String> response = githubService.reattemptSession(principal, githubId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 8. Fake Submit Session
    @PatchMapping("/questions/fake/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fakeSubmitSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_id") String githubId,
            @RequestParam("question_session_id") String sessionId) {
        githubService.fakeSubmitSession(principal, githubId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 9. Generate AI Feedback
    @GetMapping("/questions/feedback")
    public ResponseEntity<ApiResponse<GithubAiResponses.GithubEvaluationResponse>> generateFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_id") String githubId,
            @RequestParam("question_session_id") String sessionId) {
        GithubAiResponses.GithubEvaluationResponse response = githubService.generateFeedback(principal, githubId,
                sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 10. Get All Sessions
    @GetMapping("/questions/sessions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllSessions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_id") String githubId) {
        Map<String, Object> response = githubService.getAllSessions(principal, githubId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 11. Get Single Session Data
    @GetMapping("/questions/data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSessionData(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_id") String githubId,
            @RequestParam("question_session_id") String sessionId) {
        Map<String, Object> response = githubService.getSessionData(principal, githubId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 12. Delete GitHub Track
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteGithub(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_id") String githubId) {
        githubService.deleteGithub(principal, githubId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 13. Delete Single Session
    @DeleteMapping("/questions/delete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_id") String githubId,
            @RequestParam("question_session_id") String sessionId) {
        githubService.deleteSession(principal, githubId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 14. Delete Answers and Reattempt
    @PutMapping("/questions/delete-reattempt")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteAndReattempt(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_id") String githubId,
            @RequestParam("question_session_id") String sessionId) {
        githubService.deleteAndReattempt(principal, githubId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 15. Combined Feedback Across Different Sessions
    @GetMapping("/progress/github")
    public ResponseEntity<ApiResponse<Map<String, Object>>> combinedFeedbackLastXSessions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_id") String githubId,
            @RequestParam("x") int x) {
        Map<String, Object> response = githubService.combinedFeedbackLastXSessions(principal, githubId, x);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 16. Combined Feedback Within Same Session Reattempts
    @GetMapping("/progress/session")
    public ResponseEntity<ApiResponse<Map<String, Object>>> combinedFeedbackSameSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_id") String githubId,
            @RequestParam("question_session_id") String sessionId,
            @RequestParam("x") int x) {
        Map<String, Object> response = githubService.combinedFeedbackSameSession(principal, githubId, sessionId, x);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 17. Get Different Combined Feedback History
    @GetMapping("/progress/github/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDifferentCombinedFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_id") String githubId) {
        Map<String, Object> response = githubService.getDifferentCombinedFeedback(principal, githubId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 18. Get Same Combined Feedback History
    @GetMapping("/progress/session/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSameCombinedFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("github_id") String githubId,
            @RequestParam("session_number") int sessionNumber) {
        Map<String, Object> response = githubService.getSameCombinedFeedback(principal, githubId, sessionNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}