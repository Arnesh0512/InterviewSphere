package com.arnesh.interviewsphere.util;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.arnesh.interviewsphere.config.AppProperties;
import com.arnesh.interviewsphere.dto.response.GithubRepoDetailsResponse;
import com.arnesh.interviewsphere.dto.response.GithubRepositoryResponse;
import com.arnesh.interviewsphere.dto.response.PreviousGithubSessionResponse;
import com.arnesh.interviewsphere.entity.QuestionSession;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.QuestionSessionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GithubUtil {

    private final QuestionSessionRepository questionSessionRepository;
    private final AppProperties appProperties;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final WebClient.Builder webClientBuilder;

    private WebClient createGithubClient() {
        return webClientBuilder
                .baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + appProperties.getGithubApiKey())
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .build();
    }

    public boolean checkUserExists(String username) {
        try {
            createGithubClient().get()
                    .uri("/users/{username}", username)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean checkRepoExists(String owner, String repoName) {
        try {
            createGithubClient().get()
                    .uri("/repos/{owner}/{repo}", owner, repoName)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    
    public List<GithubRepositoryResponse> fetchRepositories(String githubUrl) {
        if (githubUrl == null || githubUrl.isBlank()) {
            throw new CustomException("Invalid GitHub URL provided.", HttpStatus.BAD_REQUEST);
        }

        String[] parts = githubUrl.strip().replaceAll("/+$", "").split("/");
        String username = parts[parts.length - 1];

        WebClient client = createGithubClient();

        try {
            List<Map<String, Object>> repos = client.get()
                    .uri("/users/{username}/repos", username)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    })
                    .block();

            if (repos == null || repos.isEmpty()) {
                throw new CustomException("No repositories found", HttpStatus.NOT_FOUND);
            }

            List<GithubRepositoryResponse> repoList = new ArrayList<>();
            for (int i = 0; i < repos.size(); i++) {
                Map<String, Object> repo = repos.get(i);
                repoList.add(GithubRepositoryResponse.builder()
                        .number(i + 1)
                        .name((String) repo.get("name"))
                        .link((String) repo.get("html_url"))
                        .build());
            }

            return repoList;

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException("GitHub API error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    
    public GithubRepoDetailsResponse fetchRepoDetails(String selectedRepoLink) {
        if (selectedRepoLink == null || selectedRepoLink.isBlank()) {
            throw new CustomException("Invalid repository link provided.", HttpStatus.BAD_REQUEST);
        }

        String[] parts = selectedRepoLink.strip().replaceAll("/+$", "").split("/");
        if (parts.length < 2) {
            throw new CustomException("Invalid repository link format.", HttpStatus.BAD_REQUEST);
        }

        String owner = parts[parts.length - 2];
        String repoName = parts[parts.length - 1];

        WebClient client = createGithubClient();

        try {
            Map<String, Object> repoData = client.get()
                    .uri("/repos/{owner}/{repo}", owner, repoName)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            if (repoData == null) {
                throw new CustomException("Repository data not found.", HttpStatus.NOT_FOUND);
            }

            String fetchedRepoName = (String) repoData.get("name");
            String description = repoData.get("description") != null ? (String) repoData.get("description") : "";
            String languagesUrl = (String) repoData.get("languages_url");

            Map<String, Object> languages = new HashMap<>();
            if (languagesUrl != null) {
                try {
                    languages = client.get()
                            .uri(languagesUrl)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                            })
                            .block();
                } catch (Exception ignored) {
                }
            }

            // 3. Fetch Readme
            String decodedReadme = "";
            try {
                Map<String, Object> readmeResponse = client.get()
                        .uri("/repos/{owner}/{repo}/readme", owner, repoName)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                        })
                        .block();

                if (readmeResponse != null && readmeResponse.containsKey("content")) {
                    String rawBase64Content = ((String) readmeResponse.get("content")).replaceAll("\\s+", "");
                    byte[] decodedBytes = java.util.Base64.getDecoder().decode(rawBase64Content);
                    decodedReadme = new String(decodedBytes, StandardCharsets.UTF_8);
                }
            } catch (Exception ignored) {
            }

            if (description.isBlank() && decodedReadme.isBlank()) {
                throw new CustomException("Repository contains insufficient analyzable content.",
                        HttpStatus.BAD_REQUEST);
            }

            return GithubRepoDetailsResponse.builder()
                    .repoName(fetchedRepoName)
                    .description(description)
                    .languages(languages)
                    .readme(decodedReadme)
                    .build();

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException("Failed to fetch repository details: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    
    public PreviousGithubSessionResponse previousGithubSessionQuestions(UUID githubId, Integer x,
            Integer sessionNumber) {
        List<QuestionSession> allSessions;

        if (sessionNumber != null) {
            allSessions = questionSessionRepository.findByGithubTrackIdAndStatusAndSessionNumberOrderByTimestampDesc(
                    githubId, "passive", sessionNumber);
        } else {
            allSessions = questionSessionRepository.findByGithubTrackIdAndStatusOrderByTimestampDesc(
                    githubId, "passive");
        }

        if (sessionNumber == null) {
            Map<Integer, QuestionSession> latestSessionsMap = new LinkedHashMap<>();
            for (QuestionSession session : allSessions) {
                latestSessionsMap.putIfAbsent(session.getSessionNumber(), session);
            }
            allSessions = new ArrayList<>(latestSessionsMap.values());
        }

        allSessions.sort(Comparator.comparing((QuestionSession s) -> s.getTimestamp()).reversed());

        if (x != null) {
            if (allSessions.size() > x) {
                allSessions = allSessions.subList(0, x);
            }
            if (allSessions.size() <= 1) {
                throw new CustomException("Need at least 2 sessions for combined feedback.", HttpStatus.BAD_REQUEST);
            }
        }

        Map<String, Map<String, String>> sessionDict = new LinkedHashMap<>();
        Map<String, Map<String, Object>> sessionsUsed = new LinkedHashMap<>();

        for (int idx = 0; idx < allSessions.size(); idx++) {
            QuestionSession s = allSessions.get(idx);
            Map<String, String> questionAnswerMap = new LinkedHashMap<>();

            if (s.getQuestionBank() != null) {
                for (QuestionSession.Question q : s.getQuestionBank()) {
                        questionAnswerMap.put(q.getQuestion(), q.getAnswer());
                }
            }

            sessionDict.put("session_" + (idx + 1), questionAnswerMap);

            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("session_number", s.getSessionNumber());
            sessionInfo.put("session_id", s.getId().toString());
            sessionsUsed.put(s.getTimestamp().toString(), sessionInfo);
        }

        return PreviousGithubSessionResponse.builder()
                .sessionDict(sessionDict)
                .sessionsUsed(sessionsUsed)
                .build();
    }

    
    public void autoSubmit(
            UUID githubId,
            UUID questionSessionId,
            String token,
            OffsetDateTime startTime,
            int durationInMinutes,
            Consumer<AutoSubmitContext> submitCallback) {
        OffsetDateTime endTime = startTime.plusMinutes(durationInMinutes).plusMinutes(1);
        OffsetDateTime now = OffsetDateTime.now();

        long waitSeconds = Duration.between(now, endTime).getSeconds();

        Runnable task = () -> submitCallback.accept(
                new AutoSubmitContext(githubId, questionSessionId, OffsetDateTime.now(), token));

        if (waitSeconds > 0) {
            taskScheduler.schedule(task, endTime.toInstant());
        } else {
            task.run();
        }
    }

    public record AutoSubmitContext(
            UUID githubId,
            UUID questionSessionId,
            OffsetDateTime submitTime,
            String token) {
    }
}