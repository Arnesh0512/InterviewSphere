package com.arnesh.interviewsphere.verification;

import com.arnesh.interviewsphere.entity.Candidate;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.CandidateRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidateVerificationService {

    private final CandidateRepository candidateRepository;

    @Data
    @Builder
    public static class VerifiedCandidateResult {
        private Candidate candidate;
        private UUID candidateId;
        private String email;
    }

    /**
     * Verifies token payload for candidate permissions[cite: 19].
     */
    public VerifiedCandidateResult verifyCandidatePayload(Map<String, Object> payload) {
        String candidateIdStr = (String) payload.get("candidate_id");
        String email = (String) payload.get("email");
        String role = (String) payload.get("role");

        if (candidateIdStr == null || email == null || role == null) {
            throw new CustomException("Invalid token payload", HttpStatus.UNAUTHORIZED);
        }

        if (!"candidate".equalsIgnoreCase(role)) {
            throw new CustomException("Not a candidate", HttpStatus.FORBIDDEN);
        }

        return verifyCandidate(candidateIdStr, email, "Y");
    }

    /**
     * Verifies candidate existence by ID and email[cite: 19].
     */
    public VerifiedCandidateResult verifyCandidate(String candidateIdStr, String email, String type) {
        String normalizedEmail = email.toLowerCase();
        UUID candidateId = parseUuid(candidateIdStr);

        Candidate candidate = candidateRepository.findByIdAndEmail(candidateId, normalizedEmail).orElse(null);

        if ("N".equalsIgnoreCase(type) && candidate != null) {
            throw new CustomException("candidate Already exists", HttpStatus.CONFLICT);
        }

        if ("Y".equalsIgnoreCase(type) && candidate == null) {
            throw new CustomException("candidate not found", HttpStatus.NOT_FOUND);
        }

        return VerifiedCandidateResult.builder()
                .candidate(candidate)
                .candidateId(candidateId)
                .email(normalizedEmail)
                .build();
    }

    /**
     * Verifies candidate existence by email alone[cite: 19].
     */
    public VerifiedCandidateResult verifyCandidateByEmail(String email, String type) {
        String normalizedEmail = email.toLowerCase();
        Candidate candidate = candidateRepository.findByEmail(normalizedEmail).orElse(null);

        if ("N".equalsIgnoreCase(type)) {
            if (candidate != null) {
                throw new CustomException("candidate Already exists", HttpStatus.CONFLICT);
            }
            return VerifiedCandidateResult.builder()
                    .candidate(null)
                    .candidateId(null)
                    .email(normalizedEmail)
                    .build();
        }

        if ("Y".equalsIgnoreCase(type) && candidate == null) {
            throw new CustomException("candidate not found", HttpStatus.NOT_FOUND);
        }

        return VerifiedCandidateResult.builder()
                .candidate(candidate)
                .candidateId(candidate != null ? candidate.getId() : null)
                .email(normalizedEmail)
                .build();
    }

    /**
     * Verifies candidate existence by UUID alone[cite: 19].
     */
    public VerifiedCandidateResult verifyCandidateById(String candidateIdStr, String type) {
        UUID candidateId = parseUuid(candidateIdStr);
        Candidate candidate = candidateRepository.findById(candidateId).orElse(null);

        if ("N".equalsIgnoreCase(type)) {
            if (candidate != null) {
                throw new CustomException("candidate Already exists", HttpStatus.CONFLICT);
            }
            return VerifiedCandidateResult.builder()
                    .candidate(null)
                    .candidateId(candidateId)
                    .email(null)
                    .build();
        }

        if ("Y".equalsIgnoreCase(type) && candidate == null) {
            throw new CustomException("candidate not found", HttpStatus.NOT_FOUND);
        }

        return VerifiedCandidateResult.builder()
                .candidate(candidate)
                .candidateId(candidateId)
                .email(candidate != null ? candidate.getEmail() : null)
                .build();
    }

    private UUID parseUuid(String idStr) {
        try {
            return UUID.fromString(idStr);
        } catch (Exception e) {
            throw new CustomException("Invalid candidate id", HttpStatus.UNAUTHORIZED);
        }
    }
}