package com.arnesh.interviewsphere.controller;

import com.arnesh.interviewsphere.constant.Topic;
import com.arnesh.interviewsphere.dto.request.SaveAnswerRequest;
import com.arnesh.interviewsphere.dto.request.SubmitSessionRequest;
import com.arnesh.interviewsphere.dto.response.ApiResponse;
import com.arnesh.interviewsphere.dto.response.ConceptAiResponses;
import com.arnesh.interviewsphere.dto.response.QuestionSessionResponse;
import com.arnesh.interviewsphere.security.UserPrincipal;
import com.arnesh.interviewsphere.service.ConceptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/concept")
@RequiredArgsConstructor
public class ConceptController {

    private final ConceptService conceptService;

    // 1. Start Concept Track[cite: 39]
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<Map<String, Object>>> startConcept(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody List<Topic> topics
    ) {
        Map<String, Object> response = conceptService.startConcept(principal, topics);
        return ResponseEntity.ok(ApiResponse.success("Concept track started", response));
    }

    // 2. Get All Concept Tracks[cite: 39]
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllConcepts(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<Map<String, Object>> concepts = conceptService.getAllConcepts(principal);
        return ResponseEntity.ok(ApiResponse.success(concepts));
    }

    // 3. Submit Session[cite: 39]
    @PostMapping("/questions/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("concept_id") String conceptId,
            @RequestParam("question_session_id") String sessionId,
            @Valid @RequestBody SubmitSessionRequest request
    ) {
        conceptService.submitSession(principal, conceptId, sessionId, request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 4. Generate New Questions Session[cite: 39]
    @PostMapping("/questions/new")
    public ResponseEntity<ApiResponse<QuestionSessionResponse>> generateQuestions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("concept_id") String conceptId,
            @RequestParam(value = "num_questions", defaultValue = "3") int numQuestions
    ) {
        QuestionSessionResponse response = conceptService.generateQuestions(principal, conceptId, numQuestions);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 5. Save Single Answer[cite: 39]
    @PostMapping("/questions/save")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveAnswer(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("concept_id") String conceptId,
            @RequestParam("question_session_id") String sessionId,
            @Valid @RequestBody SaveAnswerRequest request
    ) {
        conceptService.saveAnswer(principal, conceptId, sessionId, request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 6. Reattempt Question Session[cite: 39]
    @PutMapping("/questions/reattempt")
    public ResponseEntity<ApiResponse<Map<String, String>>> reattemptSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("concept_id") String conceptId,
            @RequestParam("question_session_id") String sessionId
    ) {
        Map<String, String> response = conceptService.reattemptSession(principal, conceptId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 7. Fake Submit Session[cite: 39]
    @PatchMapping("/questions/fake/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fakeSubmitSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("concept_id") String conceptId,
            @RequestParam("question_session_id") String sessionId
    ) {
        conceptService.fakeSubmitSession(principal, conceptId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 8. Generate AI Feedback[cite: 39]
    @GetMapping("/questions/feedback")
    public ResponseEntity<ApiResponse<ConceptAiResponses.ConceptEvaluationResponse>> generateFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("concept_id") String conceptId,
            @RequestParam("question_session_id") String sessionId
    ) {
        ConceptAiResponses.ConceptEvaluationResponse response = conceptService.generateFeedback(principal, conceptId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 9. Get All Sessions[cite: 39]
    @GetMapping("/questions/sessions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllSessions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("concept_id") String conceptId
    ) {
        Map<String, Object> response = conceptService.getAllSessions(principal, conceptId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 10. Get Single Session Data[cite: 39]
    @GetMapping("/questions/data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSessionData(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("concept_id") String conceptId,
            @RequestParam("question_session_id") String sessionId
    ) {
        Map<String, Object> response = conceptService.getSessionData(principal, conceptId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 11. Delete Concept Track[cite: 39]
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteConcept(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("concept_id") String conceptId
    ) {
        conceptService.deleteConcept(principal, conceptId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 12. Delete Single Session[cite: 39]
    @DeleteMapping("/questions/delete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("concept_id") String conceptId,
            @RequestParam("question_session_id") String sessionId
    ) {
        conceptService.deleteSession(principal, conceptId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 13. Delete Answers and Reattempt[cite: 39]
    @PutMapping("/questions/delete-reattempt")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteAndReattempt(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("concept_id") String conceptId,
            @RequestParam("question_session_id") String sessionId
    ) {
        conceptService.deleteAndReattempt(principal, conceptId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 14. Combined Feedback Across Different Sessions[cite: 39]
    @GetMapping("/progress/concept")
    public ResponseEntity<ApiResponse<Map<String, Object>>> combinedFeedbackLastXSessions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("concept_id") String conceptId,
            @RequestParam("x") int x
    ) {
        Map<String, Object> response = conceptService.combinedFeedbackLastXSessions(principal, conceptId, x);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 15. Combined Feedback Within Same Session Reattempts[cite: 39]
    @GetMapping("/progress/session")
    public ResponseEntity<ApiResponse<Map<String, Object>>> combinedFeedbackSameSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("concept_id") String conceptId,
            @RequestParam("question_session_id") String sessionId,
            @RequestParam("x") int x
    ) {
        Map<String, Object> response = conceptService.combinedFeedbackSameSession(principal, conceptId, sessionId, x);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 16. Get Different Combined Feedback History[cite: 39]
    @GetMapping("/progress/concept/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDifferentCombinedFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("concept_id") String conceptId
    ) {
        Map<String, Object> response = conceptService.getDifferentCombinedFeedback(principal, conceptId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 17. Get Same Combined Feedback History[cite: 39]
    @GetMapping("/progress/session/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSameCombinedFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("concept_id") String conceptId,
            @RequestParam("session_number") int sessionNumber
    ) {
        Map<String, Object> response = conceptService.getSameCombinedFeedback(principal, conceptId, sessionNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}