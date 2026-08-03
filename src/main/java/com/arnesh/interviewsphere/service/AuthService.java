package com.arnesh.interviewsphere.service;

import com.arnesh.interviewsphere.entity.Admin;
import com.arnesh.interviewsphere.entity.Candidate;
import com.arnesh.interviewsphere.repository.AdminRepository;
import com.arnesh.interviewsphere.repository.CandidateRepository;
import com.arnesh.interviewsphere.security.GoogleAuthService;
import com.arnesh.interviewsphere.security.JwtProvider;
import com.arnesh.interviewsphere.util.TimeUtil;
import com.arnesh.interviewsphere.verification.AdminVerificationService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleAuthService googleAuthService;
    private final CandidateRepository candidateRepository;
    private final AdminRepository adminRepository;
    private final AdminVerificationService adminVerificationService;
    private final JwtProvider jwtProvider;

    @Transactional
    public Map<String, String> googleAuthCandidate(Map<String, Object> data) {
        GoogleIdToken.Payload idinfo = googleAuthService.verifyGoogleToken(data);
        String email = idinfo.getEmail().toLowerCase();
        String profilePic = (String) idinfo.get("picture");

        Optional<Candidate> candidateOpt = candidateRepository.findByEmail(email);
        String candidateId;

        if (candidateOpt.isPresent()) {
            candidateId = candidateOpt.get().getId().toString();
        } else {
            Candidate candidate = Candidate.builder()
                    .email(email)
                    .profilePic(profilePic)
                    .createdOn(TimeUtil.generateTimestamp())
                    .build();
            Candidate savedCandidate = candidateRepository.save(candidate);
            candidateId = savedCandidate.getId().toString();
        }

        long expEpochSeconds = calculateExpiryTimestamp();

        Map<String, Object> payload = new HashMap<>();
        payload.put("candidate_id", candidateId);
        payload.put("email", email);
        payload.put("role", "candidate");
        payload.put("exp", expEpochSeconds);

        String accessToken = jwtProvider.createAccessToken(payload);

        return Map.of(
                "access_token", accessToken,
                "token_type", "bearer");
    }

    @Transactional
    public Map<String, String> googleAuthAdmin(Map<String, Object> data) {
        GoogleIdToken.Payload idinfo = googleAuthService.verifyGoogleToken(data);
        String email = idinfo.getEmail().toLowerCase();
        String profilePic = (String) idinfo.get("picture");

        var verifiedAdmin = adminVerificationService.verifyAdminByEmail(email, "Y");
        Admin admin = verifiedAdmin.getAdmin();

        if (profilePic != null && !profilePic.isBlank()) {
            admin.setProfilePic(profilePic);
            adminRepository.save(admin);
        }

        long expEpochSeconds = calculateExpiryTimestamp();

        Map<String, Object> payload = new HashMap<>();
        payload.put("admin_id", admin.getId().toString());
        payload.put("email", email);
        payload.put("role", "admin");
        payload.put("exp", expEpochSeconds);

        String accessToken = jwtProvider.createAccessToken(payload);

        return Map.of(
                "access_token", accessToken,
                "token_type", "bearer");
    }

    private long calculateExpiryTimestamp() {
        LocalDate today = LocalDate.now();
        int year = today.getYear() + (today.getMonthValue() > 6 ? 1 : 0);
        return LocalDateTime.of(year, 6, 30, 18, 30)
                .toEpochSecond(ZoneOffset.UTC);
    }
}