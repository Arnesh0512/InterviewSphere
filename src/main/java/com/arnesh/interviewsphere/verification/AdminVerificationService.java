package com.arnesh.interviewsphere.verification;

import com.arnesh.interviewsphere.entity.Admin;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.AdminRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminVerificationService {

    private final AdminRepository adminRepository;

    @Data
    @Builder
    public static class VerifiedAdminResult {
        private Admin admin;
        private UUID adminId;
        private String email;
    }

    /**
     * Verifies token payload for admin permissions[cite: 18].
     */
    public VerifiedAdminResult verifyAdminPayload(Map<String, Object> payload) {
        String adminIdStr = (String) payload.get("admin_id");
        String email = (String) payload.get("email");
        String role = (String) payload.get("role");

        if (adminIdStr == null || email == null || role == null) {
            throw new CustomException("Invalid token payload", HttpStatus.UNAUTHORIZED);
        }

        if (!"admin".equals(role)) {
            throw new CustomException("Not a admin", HttpStatus.FORBIDDEN);
        }

        return verifyAdmin(adminIdStr, email, "Y");
    }

    /**
     * Verifies admin existence by ID and email[cite: 18].
     */
    public VerifiedAdminResult verifyAdmin(String adminIdStr, String email, String type) {
        String normalizedEmail = email.toLowerCase();
        UUID adminId = parseUuid(adminIdStr);

        Admin admin = adminRepository.findByIdAndEmail(adminId, normalizedEmail).orElse(null);

        if ("N".equalsIgnoreCase(type) && admin != null) {
            throw new CustomException("Admin Already exists", HttpStatus.CONFLICT);
        }

        if ("Y".equalsIgnoreCase(type) && admin == null) {
            throw new CustomException("Admin not found", HttpStatus.NOT_FOUND);
        }

        return VerifiedAdminResult.builder()
                .admin(admin)
                .adminId(adminId)
                .email(normalizedEmail)
                .build();
    }

    /**
     * Verifies admin existence by email alone[cite: 18].
     */
    public VerifiedAdminResult verifyAdminByEmail(String email, String type) {
        String normalizedEmail = email.toLowerCase();
        Admin admin = adminRepository.findByEmail(normalizedEmail).orElse(null);

        if ("N".equalsIgnoreCase(type)) {
            if (admin != null) {
                throw new CustomException("Admin Already exists", HttpStatus.CONFLICT);
            }
            return VerifiedAdminResult.builder()
                    .admin(null)
                    .adminId(null)
                    .email(normalizedEmail)
                    .build();
        }

        if ("Y".equalsIgnoreCase(type) && admin == null) {
            throw new CustomException("Admin not found", HttpStatus.NOT_FOUND);
        }

        return VerifiedAdminResult.builder()
                .admin(admin)
                .adminId(admin != null ? admin.getId() : null)
                .email(normalizedEmail)
                .build();
    }

    /**
     * Verifies admin existence by UUID alone[cite: 18].
     */
    public VerifiedAdminResult verifyAdminById(String adminIdStr, String type) {
        UUID adminId = parseUuid(adminIdStr);
        Admin admin = adminRepository.findById(adminId).orElse(null);

        if ("N".equalsIgnoreCase(type)) {
            if (admin != null) {
                throw new CustomException("Admin Already exists", HttpStatus.CONFLICT);
            }
            return VerifiedAdminResult.builder()
                    .admin(null)
                    .adminId(adminId)
                    .email(null)
                    .build();
        }

        if ("Y".equalsIgnoreCase(type) && admin == null) {
            throw new CustomException("Admin not found", HttpStatus.NOT_FOUND);
        }

        return VerifiedAdminResult.builder()
                .admin(admin)
                .adminId(adminId)
                .email(admin != null ? admin.getEmail() : null)
                .build();
    }

    private UUID parseUuid(String idStr) {
        try {
            return UUID.fromString(idStr);
        } catch (Exception e) {
            throw new CustomException("Invalid admin id", HttpStatus.UNAUTHORIZED);
        }
    }
}