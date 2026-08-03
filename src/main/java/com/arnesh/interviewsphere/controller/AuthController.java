package com.arnesh.interviewsphere.controller;

import com.arnesh.interviewsphere.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/candidate")
    public ResponseEntity<Map<String, String>> googleAuthCandidate(@RequestBody Map<String, Object> data) {
        Map<String, String> response = authService.googleAuthCandidate(data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin")
    public ResponseEntity<Map<String, String>> googleAuthAdmin(@RequestBody Map<String, Object> data) {
        Map<String, String> response = authService.googleAuthAdmin(data);
        return ResponseEntity.ok(response);
    }
}