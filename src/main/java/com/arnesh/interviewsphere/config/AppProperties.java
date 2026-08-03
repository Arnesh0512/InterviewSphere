package com.arnesh.interviewsphere.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class AppProperties {

    @Value("${spring.datasource.url:}")
    private String databaseUrl;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${spring.ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${github.api.key:}")
    private String githubApiKey;
}