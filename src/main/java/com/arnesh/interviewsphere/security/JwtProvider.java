package com.arnesh.interviewsphere.security;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.arnesh.interviewsphere.exception.CustomException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encodes payload dictionary into a signed JWT token.
     */
    public String createAccessToken(Map<String, Object> payload) {
        return Jwts.builder()
                .claims(payload)
                .signWith(getSigningKey()) // JJWT 0.12+ infers algorithm automatically from SecretKey
                .compact();
    }

    /**
     * Decodes and verifies JWT token signature and expiration.
     */
    public Claims verifyAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (claims.getExpiration() == null) {
                throw new CustomException("Token has no expiry", HttpStatus.UNAUTHORIZED);
            }

            return claims;

        } catch (CustomException ce) {
            throw ce;
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException("Invalid or expired access token", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Generates a signed JWT access token for a user ID, email, and role.
     */
    public String generateAccessToken(java.util.UUID id, String email, String role) {
        Map<String, Object> payload = new java.util.HashMap<>();
        String idKey = "admin".equalsIgnoreCase(role) ? "admin_id" : "candidate_id";
        payload.put(idKey, id.toString());
        payload.put("email", email);
        payload.put("role", role);

        // Setting an expiration matching dev expectations (~June 30 of current/next year)
        java.time.LocalDate today = java.time.LocalDate.now();
        int year = today.getYear() + (today.getMonthValue() > 6 ? 1 : 0);
        java.util.Date expDate = java.util.Date.from(
                java.time.LocalDateTime.of(year, 6, 30, 18, 30)
                        .atZone(java.time.ZoneId.of("UTC"))
                        .toInstant()
        );
        payload.put("exp", expDate);

        return createAccessToken(payload);
    }
}
