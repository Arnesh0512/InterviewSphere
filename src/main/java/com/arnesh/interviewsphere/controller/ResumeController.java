package com.arnesh.interviewsphere.controller;

import com.arnesh.interviewsphere.dto.request.SaveAnswerRequest;
import com.arnesh.interviewsphere.dto.request.SubmitSessionRequest;
import com.arnesh.interviewsphere.dto.response.ApiResponse;
import com.arnesh.interviewsphere.dto.response.QuestionSessionResponse;
import com.arnesh.interviewsphere.dto.response.ResumeAiResponses;
import com.arnesh.interviewsphere.entity.PracticeResumeTrack;
import com.arnesh.interviewsphere.security.UserPrincipal;
import com.arnesh.interviewsphere.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    // 1. Upload Resume
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "ocr_mode", defaultValue = "N") String ocrMode) {
        Map<String, Object> response = resumeService.uploadResume(principal, file, ocrMode);
        return ResponseEntity.ok(ApiResponse.success("Resume uploaded successfully", response));
    }

    // 2. Get All Resumes
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllResumes(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Map<String, Object>> resumes = resumeService.getAllResumes(principal);
        return ResponseEntity.ok(ApiResponse.success(resumes));
    }

    // 3. Get Resume Track File Metadata
    @GetMapping("/file")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResumeFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId) {
        PracticeResumeTrack track = resumeService.getResumeTrack(principal, resumeId);
        
        Map<String, Object> filteredData = Map.of(
            "id", track.getId(),
            "file_url", track.getFileUrl(),
            "filename", track.getFilename()
        );

        return ResponseEntity.ok(ApiResponse.success(filteredData));
    }

    // 4. Generate Questions Session
    @PostMapping("/questions/new")
    public ResponseEntity<ApiResponse<QuestionSessionResponse>> generateQuestions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId,
            @RequestParam(value = "num_questions", defaultValue = "3") int numQuestions) {
        QuestionSessionResponse response = resumeService.generateQuestions(principal, resumeId, numQuestions);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 5. Submit Session
    @PostMapping("/questions/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId,
            @RequestParam("question_session_id") String sessionId,
            @Valid @RequestBody SubmitSessionRequest request) {
        resumeService.submitSession(principal, resumeId, sessionId, request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 6. Save Answer
    @PostMapping("/questions/save")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveAnswer(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId,
            @RequestParam("question_session_id") String sessionId,
            @Valid @RequestBody SaveAnswerRequest request) {
        resumeService.saveAnswer(principal, resumeId, sessionId, request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 7. Reattempt Session
    @PutMapping("/questions/reattempt")
    public ResponseEntity<ApiResponse<Map<String, String>>> reattemptSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId,
            @RequestParam("question_session_id") String sessionId) {
        Map<String, String> response = resumeService.reattemptSession(principal, resumeId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 8. Fake Submit Session
    @PatchMapping("/questions/fake/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fakeSubmitSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId,
            @RequestParam("question_session_id") String sessionId) {
        resumeService.fakeSubmitSession(principal, resumeId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 9. Generate AI Feedback
    @GetMapping("/questions/feedback")
    public ResponseEntity<ApiResponse<ResumeAiResponses.ResumeEvaluationResponse>> generateFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId,
            @RequestParam("question_session_id") String sessionId) {
        ResumeAiResponses.ResumeEvaluationResponse response = resumeService.generateFeedback(principal, resumeId,
                sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 10. Get All Sessions
    @GetMapping("/questions/sessions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllSessions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId) {
        Map<String, Object> response = resumeService.getAllSessions(principal, resumeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 11. Get Session Data
    @GetMapping("/questions/data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSessionData(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId,
            @RequestParam("question_session_id") String sessionId) {
        Map<String, Object> response = resumeService.getSessionData(principal, resumeId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 12. Delete Resume
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId) {
        resumeService.deleteResume(principal, resumeId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 13. Delete Single Session
    @DeleteMapping("/questions/delete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId,
            @RequestParam("question_session_id") String sessionId) {
        resumeService.deleteSession(principal, resumeId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 14. Delete Answers and Reattempt
    @PutMapping("/questions/delete-reattempt")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteAndReattempt(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId,
            @RequestParam("question_session_id") String sessionId) {
        resumeService.deleteAndReattempt(principal, resumeId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    // 15. Combined Feedback Across Different Sessions
    @GetMapping("/progress/resume")
    public ResponseEntity<ApiResponse<Map<String, Object>>> combinedFeedbackLastXSessions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId,
            @RequestParam("x") int x) {
        Map<String, Object> response = resumeService.combinedFeedbackLastXSessions(principal, resumeId, x);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 16. Combined Feedback Within Same Session Reattempts
    @GetMapping("/progress/session")
    public ResponseEntity<ApiResponse<Map<String, Object>>> combinedFeedbackSameSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId,
            @RequestParam("question_session_id") String sessionId,
            @RequestParam("x") int x) {
        Map<String, Object> response = resumeService.combinedFeedbackSameSession(principal, resumeId, sessionId, x);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 17. Get Different Combined Feedback History
    @GetMapping("/progress/resume/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDifferentCombinedFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId) {
        Map<String, Object> response = resumeService.getDifferentCombinedFeedback(principal, resumeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 18. Get Same Combined Feedback History
    @GetMapping("/progress/session/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSameCombinedFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("resume_id") String resumeId,
            @RequestParam("session_number") int sessionNumber) {
        Map<String, Object> response = resumeService.getSameCombinedFeedback(principal, resumeId, sessionNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}