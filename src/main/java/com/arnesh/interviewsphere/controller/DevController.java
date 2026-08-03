package com.arnesh.interviewsphere.controller;

import com.arnesh.interviewsphere.dto.response.ApiResponse;
import com.arnesh.interviewsphere.service.DevService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevController {

    private final DevService devService;

    @PostMapping("/generate-candidate-token")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateCandidateToken(
            @RequestParam("email") String email
    ) {
        Map<String, String> response = devService.generateCandidateToken(email);
        return ResponseEntity.ok(ApiResponse.success("Token generated successfully", response));
    }

    @PostMapping("/generate-admin-token")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateAdminToken(
            @RequestParam("email") String email
    ) {
        Map<String, String> response = devService.generateAdminToken(email);
        return ResponseEntity.ok(ApiResponse.success("Token generated successfully", response));
    }
}