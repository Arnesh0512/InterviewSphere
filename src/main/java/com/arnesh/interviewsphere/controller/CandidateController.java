package com.arnesh.interviewsphere.controller;

import com.arnesh.interviewsphere.dto.request.UserCreateRequest;
import com.arnesh.interviewsphere.security.UserPrincipal;
import com.arnesh.interviewsphere.service.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    @PatchMapping("/register")
    public ResponseEntity<Map<String, Object>> registerCandidate(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserCreateRequest candidateData) {
        Map<String, Object> response = candidateService.registerCandidate(principal, candidateData);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-details")
    public ResponseEntity<Map<String, Object>> changeCandidateDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserCreateRequest candidateData) {
        Map<String, Object> response = candidateService.changeCandidateDetails(principal, candidateData);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getCandidateProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        Map<String, Object> response = candidateService.getCandidateProfile(principal);
        return ResponseEntity.ok(response);
    }
}