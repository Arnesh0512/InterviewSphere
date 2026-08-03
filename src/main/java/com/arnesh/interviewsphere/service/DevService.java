package com.arnesh.interviewsphere.service;

import com.arnesh.interviewsphere.entity.Admin;
import com.arnesh.interviewsphere.entity.Candidate;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.AdminRepository;
import com.arnesh.interviewsphere.repository.CandidateRepository;
import com.arnesh.interviewsphere.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DevService {

    private final CandidateRepository candidateRepository;
    private final AdminRepository adminRepository;
    private final JwtProvider jwtProvider;

    private Date getDefaultExpiration() {
        LocalDate today = LocalDate.now();
        int year = today.getYear() + (today.getMonthValue() > 6 ? 1 : 0);
        return Date.from(LocalDateTime.of(year, 6, 30, 18, 30)
                .atZone(ZoneId.of("UTC")).toInstant());
    }

    public Map<String, String> generateCandidateToken(String email) {
        Candidate candidate = candidateRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new CustomException("Candidate not found", HttpStatus.NOT_FOUND));

        Map<String, Object> payload = Map.of(
                "candidate_id", candidate.getId().toString(),
                "email", email,
                "role", "candidate",
                "exp", getDefaultExpiration()
        );

        String token = jwtProvider.createAccessToken(payload);
        return Map.of("access_token", token);
    }

    public Map<String, String> generateAdminToken(String email) {
        Admin admin = adminRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new CustomException("Admin not found", HttpStatus.NOT_FOUND));

        Map<String, Object> payload = Map.of(
                "admin_id", admin.getId().toString(),
                "email", email,
                "role", "admin",
                "exp", getDefaultExpiration()
        );

        String token = jwtProvider.createAccessToken(payload);
        return Map.of("access_token", token);
    }
}