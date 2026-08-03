package com.arnesh.interviewsphere.controller;

import com.arnesh.interviewsphere.dto.response.ApiResponse;
import com.arnesh.interviewsphere.entity.Contest;
import com.arnesh.interviewsphere.security.UserPrincipal;
import com.arnesh.interviewsphere.service.ContestService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contest")
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;

    // 1. Fetch Available Contests
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> fetchAvailableContests(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<Map<String, Object>> contests = contestService.fetchAvailableContests(principal);
        return ResponseEntity.ok(ApiResponse.success(contests));
    }

    // 2. Fetch Contest Details
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<Contest>> fetchContestDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        Contest contest = contestService.fetchContestDetails(principal, contestId);
        return ResponseEntity.ok(ApiResponse.success(contest));
    }

    // 3. Register for Contest
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerForContest(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        Map<String, Object> response = contestService.registerForContest(principal, contestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 4. Unregister from Contest
    @DeleteMapping("/unregister")
    public ResponseEntity<ApiResponse<Map<String, Object>>> unregisterFromContest(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        Map<String, Object> response = contestService.unregisterFromContest(principal, contestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 5. Submit Resume Round
    @PostMapping("/resume/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId,
            @RequestParam("frontend_timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime frontendTimestamp,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "ocr_mode", defaultValue = "N") String ocrMode
    ) {
        Map<String, Object> response = contestService.submitResume(principal, contestId, frontendTimestamp, file, ocrMode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 6. Resume Round Leaderboard
    @GetMapping("/leaderboard/resume")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResumeLeaderboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        Map<String, Object> response = contestService.getResumeLeaderboard(principal, contestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 7. Submit Coding Round
    @PostMapping("/coding/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitCoding(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId,
            @RequestParam("frontend_timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime frontendTimestamp
    ) {
        Map<String, Object> response = contestService.submitCoding(principal, contestId, frontendTimestamp);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 8. Get Coding Questions
    @GetMapping("/coding/questions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCodingQuestions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        Map<String, Object> response = contestService.getCodingQuestions(principal, contestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 9. Save Coding Answer
    @PostMapping("/coding/answer")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitCodingAnswer(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId,
            @RequestParam("question_id") String questionId,
            @RequestParam("answer") String answer,
            @RequestParam("language") String language,
            @RequestParam("frontend_timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime frontendTimestamp
    ) {
        Map<String, Object> response = contestService.submitCodingAnswer(principal, contestId, questionId, answer, language, frontendTimestamp);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 10. Coding Leaderboard
    @GetMapping("/leaderboard/coding")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCodingLeaderboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        Map<String, Object> response = contestService.getCodingLeaderboard(principal, contestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 11. Submit Concept Round
    @PostMapping("/concept/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitConcept(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId,
            @RequestParam("frontend_timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime frontendTimestamp
    ) {
        Map<String, Object> response = contestService.submitConcept(principal, contestId, frontendTimestamp);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 12. Submit HR Round
    @PostMapping("/hr/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitHr(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId,
            @RequestParam("frontend_timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime frontendTimestamp
    ) {
        Map<String, Object> response = contestService.submitHr(principal, contestId, frontendTimestamp);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 13. Final Leaderboard
    @GetMapping("/leaderboard/final")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFinalLeaderboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        Map<String, Object> response = contestService.getFinalLeaderboard(principal, contestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 14. Resume Question Bank
    @GetMapping("/resume/question-bank")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResumeQuestionBank(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        Map<String, Object> response = contestService.getResumeQuestionBank(principal, contestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 15. Coding Question Bank
    @GetMapping("/coding/question-bank")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCodingQuestionBank(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        Map<String, Object> response = contestService.getCodingQuestionBank(principal, contestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 16. Concept Question Bank
    @GetMapping("/concept/question-bank")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConceptQuestionBank(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        Map<String, Object> response = contestService.getConceptQuestionBank(principal, contestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 17. HR Question Bank
    @GetMapping("/hr/question-bank")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHrQuestionBank(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        Map<String, Object> response = contestService.getHrQuestionBank(principal, contestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}