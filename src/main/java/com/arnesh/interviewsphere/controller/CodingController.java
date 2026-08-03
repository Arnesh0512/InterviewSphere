package com.arnesh.interviewsphere.controller;

import com.arnesh.interviewsphere.dto.request.SaveAnswerRequest;
import com.arnesh.interviewsphere.dto.request.SubmitSessionRequest;
import com.arnesh.interviewsphere.dto.response.ApiResponse;
import com.arnesh.interviewsphere.dto.response.CodingAiResponses;
import com.arnesh.interviewsphere.security.UserPrincipal;
import com.arnesh.interviewsphere.service.CodingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coding")
@RequiredArgsConstructor
public class CodingController {

    private final CodingService codingService;

    // 1. Start Coding Track[cite: 50]
    @PostMapping("/start-coding")
    public ResponseEntity<ApiResponse<Map<String, Object>>> startCoding(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(value = "company", required = false) List<String> company,
            @RequestParam(value = "tag", required = false) List<String> tag,
            @RequestParam(value = "difficulty", required = false) List<String> difficulty
    ) {
        Map<String, Object> response = codingService.startCoding(principal, company, tag, difficulty);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 2. Get All Coding Tracks[cite: 50]
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllCodings(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<Map<String, Object>> codings = codingService.getAllCodings(principal);
        return ResponseEntity.ok(ApiResponse.success(codings));
    }

    // 3. Submit Session[cite: 50]
    @PostMapping("/questions/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("coding_id") String codingId,
            @RequestParam("question_session_id") String sessionId,
            @Valid @RequestBody SubmitSessionRequest request
    ) {
        codingService.submitSession(principal, codingId, sessionId, request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 4. Generate New Questions Session[cite: 50]
    @PostMapping("/questions/new")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateQuestions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("coding_id") String codingId,
            @RequestParam("num_questions") int numQuestions
    ) {
        Map<String, Object> response = codingService.generateQuestions(principal, codingId, numQuestions);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 5. Save Single Answer[cite: 50]
    @PostMapping("/questions/save")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveAnswer(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("coding_id") String codingId,
            @RequestParam("question_session_id") String sessionId,
            @Valid @RequestBody SaveAnswerRequest request
    ) {
        codingService.saveAnswer(principal, codingId, sessionId, request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 6. Reattempt Question Session[cite: 50]
    @PutMapping("/questions/reattempt")
    public ResponseEntity<ApiResponse<Map<String, String>>> reattemptSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("coding_id") String codingId,
            @RequestParam("question_session_id") String sessionId
    ) {
        Map<String, String> response = codingService.reattemptSession(principal, codingId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 7. Fake Submit Session[cite: 50]
    @PatchMapping("/questions/fake/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fakeSubmitSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("coding_id") String codingId,
            @RequestParam("question_session_id") String sessionId
    ) {
        codingService.fakeSubmitSession(principal, codingId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 8. Generate AI Feedback[cite: 50]
    @GetMapping("/questions/feedback")
    public ResponseEntity<ApiResponse<CodingAiResponses.CodingEvaluationResponse>> generateFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("coding_id") String codingId,
            @RequestParam("question_session_id") String sessionId
    ) {
        CodingAiResponses.CodingEvaluationResponse response = codingService.generateFeedback(principal, codingId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 9. Get All Sessions[cite: 50]
    @GetMapping("/questions/sessions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllSessions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("coding_id") String codingId
    ) {
        Map<String, Object> response = codingService.getAllSessions(principal, codingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 10. Get Single Session Data[cite: 50]
    @GetMapping("/questions/data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSessionData(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("coding_id") String codingId,
            @RequestParam("question_session_id") String sessionId
    ) {
        Map<String, Object> response = codingService.getSessionData(principal, codingId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 11. Delete Coding Track[cite: 50]
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteCoding(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("coding_id") String codingId
    ) {
        codingService.deleteCoding(principal, codingId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 12. Delete Single Session[cite: 50]
    @DeleteMapping("/questions/delete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("coding_id") String codingId,
            @RequestParam("question_session_id") String sessionId
    ) {
        codingService.deleteSession(principal, codingId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 13. Delete Answers and Reattempt[cite: 50]
    @PutMapping("/questions/delete-reattempt")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteAndReattempt(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("coding_id") String codingId,
            @RequestParam("question_session_id") String sessionId
    ) {
        codingService.deleteAndReattempt(principal, codingId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 14. Combined Feedback Across Different Sessions[cite: 50]
    @GetMapping("/progress/coding")
    public ResponseEntity<ApiResponse<Map<String, Object>>> combinedFeedbackLastXSessions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("coding_id") String codingId,
            @RequestParam("x") int x
    ) {
        Map<String, Object> response = codingService.combinedFeedbackLastXSessions(principal, codingId, x);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 15. Combined Feedback Within Same Session Reattempts[cite: 50]
    @GetMapping("/progress/session")
    public ResponseEntity<ApiResponse<Map<String, Object>>> combinedFeedbackSameSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("coding_id") String codingId,
            @RequestParam("question_session_id") String sessionId,
            @RequestParam("x") int x
    ) {
        Map<String, Object> response = codingService.combinedFeedbackSameSession(principal, codingId, sessionId, x);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 16. Get Different Combined Feedback History[cite: 50]
    @GetMapping("/progress/coding/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDifferentCombinedFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("coding_id") String codingId
    ) {
        Map<String, Object> response = codingService.getDifferentCombinedFeedback(principal, codingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 17. Get Same Combined Feedback History[cite: 50]
    @GetMapping("/progress/session/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSameCombinedFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("coding_id") String codingId,
            @RequestParam("session_number") int sessionNumber
    ) {
        Map<String, Object> response = codingService.getSameCombinedFeedback(principal, codingId, sessionNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}