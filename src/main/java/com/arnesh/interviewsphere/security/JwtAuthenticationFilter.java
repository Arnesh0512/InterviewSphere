package com.arnesh.interviewsphere.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Claims claims = jwtProvider.verifyAccessToken(token);

                String userIdStr = claims.get("id", String.class);
                if (userIdStr == null) {
                    userIdStr = claims.get("candidate_id", String.class);
                }
                if (userIdStr == null) {
                    userIdStr = claims.get("admin_id", String.class);
                }

                String email = claims.getSubject();
                if (email == null) {
                    email = claims.get("email", String.class);
                }

                String userType = claims.get("role", String.class);
                if (userType == null) {
                    userType = claims.get("userType", String.class);
                }

                if (userIdStr != null && email != null) {
                    UserPrincipal principal = UserPrincipal.builder()
                            .id(UUID.fromString(userIdStr))
                            .email(email)
                            .userType(userType != null ? userType : "candidate")
                            .build();

                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority(
                                    "ROLE_" + (userType != null ? userType.toUpperCase() : "CANDIDATE")));

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal, null, authorities);

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception ignored) {
                // Invalid token; security context remains empty for SecurityConfig to handle
            }
        }

        filterChain.doFilter(request, response);
    }
}