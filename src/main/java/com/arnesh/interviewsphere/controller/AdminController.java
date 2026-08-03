package com.arnesh.interviewsphere.controller;

import com.arnesh.interviewsphere.dto.request.ContestCreateRequest;
import com.arnesh.interviewsphere.dto.request.UserCreateRequest;
import com.arnesh.interviewsphere.dto.response.ApiResponse;
import com.arnesh.interviewsphere.dto.response.LeaderboardEntryResponse;
import com.arnesh.interviewsphere.entity.Contest;
import com.arnesh.interviewsphere.entity.ContestCandidateResponse;
import com.arnesh.interviewsphere.security.UserPrincipal;
import com.arnesh.interviewsphere.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 1. Register Admin
    @PatchMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerAdmin(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserCreateRequest request
    ) {
        Map<String, Object> response = adminService.registerAdmin(principal, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 2. Change Admin Details
    @PutMapping("/change-details")
    public ResponseEntity<ApiResponse<Map<String, Object>>> changeAdminDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserCreateRequest request
    ) {
        Map<String, Object> response = adminService.changeAdminDetails(principal, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 3. Get Profile
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminProfile(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Map<String, Object> response = adminService.getAdminProfile(principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 4. Create Contest
    @PostMapping("/create-contest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createContest(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ContestCreateRequest request
    ) {
        Map<String, Object> response = adminService.createContest(principal, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 5. Get All Contest Summaries
    @GetMapping("/contest-ids")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllContestIds(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<Map<String, Object>> response = adminService.getAllContestIds(principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 6. Get Single Contest Details
    @GetMapping("/contest")
    public ResponseEntity<ApiResponse<Contest>> getContestDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        Contest contest = adminService.getContestDetails(principal, contestId);
        return ResponseEntity.ok(ApiResponse.success(contest));
    }

    // 7. Get Registered Candidates
    @GetMapping("/contest/candidates")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRegisteredCandidates(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        List<Map<String, Object>> response = adminService.getRegisteredCandidates(principal, contestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 8. View Resume Leaderboard
    @GetMapping("/leaderboard/resume")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryResponse>>> viewResumeLeaderboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        List<LeaderboardEntryResponse> response = adminService.viewRoundLeaderboard(principal, contestId, "resume");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 9. View Coding Leaderboard
    @GetMapping("/leaderboard/coding")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryResponse>>> viewCodingLeaderboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        List<LeaderboardEntryResponse> response = adminService.viewRoundLeaderboard(principal, contestId, "coding");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 10. View Concept Leaderboard
    @GetMapping("/leaderboard/concept")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryResponse>>> viewConceptLeaderboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        List<LeaderboardEntryResponse> response = adminService.viewRoundLeaderboard(principal, contestId, "concept");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 11. View HR Leaderboard
    @GetMapping("/leaderboard/hr")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryResponse>>> viewHrLeaderboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        List<LeaderboardEntryResponse> response = adminService.viewRoundLeaderboard(principal, contestId, "hr");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 12. View Final Leaderboard
    @GetMapping("/leaderboard/final")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryResponse>>> viewFinalLeaderboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        List<LeaderboardEntryResponse> response = adminService.viewRoundLeaderboard(principal, contestId, "final");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 13. Get Candidate Contest Details
    @GetMapping("/contest/candidate")
    public ResponseEntity<ApiResponse<ContestCandidateResponse>> getCandidateContestDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId,
            @RequestParam("candidate_id") String candidateId
    ) {
        ContestCandidateResponse response = adminService.getCandidateContestDetails(principal, contestId, candidateId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 14. Delete Contest
    @DeleteMapping("/contest/delete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteContest(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("contest_id") String contestId
    ) {
        Map<String, Object> response = adminService.deleteContest(principal, contestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}