package com.arnesh.interviewsphere.security;

import com.arnesh.interviewsphere.exception.CustomException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

@Service
public class GoogleAuthService {

    @Value("${google.client.id}")
    private String googleClientId;

    
    public GoogleIdToken.Payload verifyGoogleToken(Map<String, Object> data) {
        if (data == null) {
            throw new CustomException("Invalid input", HttpStatus.BAD_REQUEST);
        }

        String token = (String) data.get("token");
        if (token == null || token.isBlank()) {
            throw new CustomException("Token missing", HttpStatus.BAD_REQUEST);
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(token);

            if (idToken == null) {
                throw new CustomException("Invalid Google token", HttpStatus.UNAUTHORIZED);
            }

            return idToken.getPayload();

        } catch (GeneralSecurityException | IOException | CustomException e) {
            throw new CustomException("Invalid Google token", HttpStatus.UNAUTHORIZED);
        }
    }
}