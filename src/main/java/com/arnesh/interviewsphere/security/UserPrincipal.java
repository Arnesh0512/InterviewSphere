package com.arnesh.interviewsphere.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPrincipal {
    private UUID id;
    private String email;
    private String userType; // "candidate" or "admin"
}