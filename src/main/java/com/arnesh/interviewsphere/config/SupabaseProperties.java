package com.arnesh.interviewsphere.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "supabase")
@Getter
@Setter
public class SupabaseProperties {

    private String url;
    private String key;
    private Bucket bucket = new Bucket();

    @Getter
    @Setter
    public static class Bucket {
        private String resumes;
        private String contestResumes;
        private String contestAudio;
        private String audio;
    }
}