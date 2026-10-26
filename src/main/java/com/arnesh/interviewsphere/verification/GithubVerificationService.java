package com.arnesh.interviewsphere.verification;

import com.arnesh.interviewsphere.entity.PracticeGithubTrack;
import com.arnesh.interviewsphere.entity.QuestionSession;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.PracticeGithubTrackRepository;
import com.arnesh.interviewsphere.repository.QuestionSessionRepository;
import com.arnesh.interviewsphere.util.GithubUtil;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GithubVerificationService {

    private final PracticeGithubTrackRepository practiceGithubTrackRepository;
    private final QuestionSessionRepository questionSessionRepository;
    private final GithubUtil githubUtil;

    @Data
    @Builder
    public static class VerifiedGithubResult {
        private PracticeGithubTrack githubTrack;
        private UUID githubId;
    }

    @Data
    @Builder
    public static class VerifiedSessionResult {
        private QuestionSession sessionDoc;
        private UUID sessionObjId;
    }

    @Data
    @Builder
    public static class VerifiedTimestampResult {
        private OffsetDateTime frontendTime;
        private OffsetDateTime backendTime;
    }

    // 1. GITHUB LINK & REPO API VERIFICATION
    public void verifyGithubLink(String githubLink) {
        if (githubLink == null || githubLink.isBlank()) {
            throw new CustomException("GitHub link required", HttpStatus.BAD_REQUEST);
        }

        String cleanedLink = githubLink.strip().replaceAll("/+$", "");

        if (!cleanedLink.contains("github.com")) {
            throw new CustomException("Invalid GitHub URL", HttpStatus.BAD_REQUEST);
        }

        String[] parts = cleanedLink.split("/");
        if (parts.length < 4) {
            throw new CustomException("Invalid GitHub profile URL", HttpStatus.BAD_REQUEST);
        }

        String username = parts[parts.length - 1];
        boolean userExists = githubUtil.checkUserExists(username);

        if (!userExists) {
            throw new CustomException("GitHub user not found", HttpStatus.NOT_FOUND);
        }
    }

    
    public void verifyGithubRepo(String repoLink) {
        if (repoLink == null || repoLink.isBlank()) {
            throw new CustomException("Repository link required", HttpStatus.BAD_REQUEST);
        }

        String cleanedLink = repoLink.strip().replaceAll("/+$", "");

        if (!cleanedLink.contains("github.com")) {
            throw new CustomException("Invalid GitHub repository URL", HttpStatus.BAD_REQUEST);
        }

        String[] parts = cleanedLink.split("/");
        if (parts.length < 5) {
            throw new CustomException("Invalid repository URL format", HttpStatus.BAD_REQUEST);
        }

        String owner = parts[parts.length - 2];
        String repoName = parts[parts.length - 1];

        boolean repoExists = githubUtil.checkRepoExists(owner, repoName);

        if (!repoExists) {
            throw new CustomException("Repository not found", HttpStatus.NOT_FOUND);
        }
    }

    
    public void verifyGithubLinkRepo(String githubLink, String repoLink) {
        verifyGithubLink(githubLink);
        verifyGithubRepo(repoLink);

        String username = githubLink.strip().replaceAll("/+$", "")
                .split("/")[githubLink.strip().replaceAll("/+$", "").split("/").length - 1];
        String repoOwner = repoLink.strip().replaceAll("/+$", "")
                .split("/")[repoLink.strip().replaceAll("/+$", "").split("/").length - 2];

        if (!username.equalsIgnoreCase(repoOwner)) {
            throw new CustomException("Repository does not belong to provided GitHub profile", HttpStatus.BAD_REQUEST);
        }
    }

    // 2. TRACK & SESSION VERIFICATION
    public VerifiedGithubResult verifyGithub(String githubIdStr, UUID candidateId) {
        UUID githubId = parseUuid(githubIdStr, "Invalid github_id", HttpStatus.BAD_REQUEST);

        PracticeGithubTrack track = practiceGithubTrackRepository
                .findByIdAndCandidateId(githubId, candidateId)
                .orElseThrow(() -> new CustomException("GitHub document not found", HttpStatus.NOT_FOUND));

        return VerifiedGithubResult.builder()
                .githubTrack(track)
                .githubId(githubId)
                .build();
    }

    
    public VerifiedSessionResult verifyQuestionSession(String questionSessionIdStr, UUID githubId) {
        UUID sessionObjId = parseUuid(questionSessionIdStr, "Invalid question_session_id", HttpStatus.BAD_REQUEST);

        QuestionSession sessionDoc = questionSessionRepository
                .findByIdAndGithubTrackId(sessionObjId, githubId)
                .orElseThrow(() -> new CustomException("Question session not found", HttpStatus.NOT_FOUND));

        return VerifiedSessionResult.builder()
                .sessionDoc(sessionDoc)
                .sessionObjId(sessionObjId)
                .build();
    }

    
    public void verifyQuestionId(QuestionSession sessionDoc, int questionId) {
        List<QuestionSession.Question> questionBank = sessionDoc.getQuestionBank() != null
                ? sessionDoc.getQuestionBank()
                : List.of();

        if (questionId < 1 || questionId > questionBank.size()) {
            throw new CustomException("Invalid question_id", HttpStatus.BAD_REQUEST);
        }
    }

    
    public void verifySessionStatus(QuestionSession sessionDoc) {
        if (!"active".equalsIgnoreCase(sessionDoc.getStatus())) {
            throw new CustomException("Session is not active", HttpStatus.BAD_REQUEST);
        }
    }

    
    public void verifySessionStatus2(QuestionSession sessionDoc) {
        if (!"passive".equalsIgnoreCase(sessionDoc.getStatus())) {
            throw new CustomException("Session is not passive", HttpStatus.BAD_REQUEST);
        }
    }

    
    public void verifySessionTime(QuestionSession sessionDoc, UUID sessionObjId) {
        OffsetDateTime startTime = sessionDoc.getTimestamp().withOffsetSameInstant(ZoneOffset.UTC);
        int totalTime = Objects.requireNonNullElse(sessionDoc.getTime(), 0);


        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        double elapsedMinutes = Duration.between(startTime, now).toSeconds() / 60.0;

        if (elapsedMinutes > totalTime) {
            sessionDoc.setStatus("passive");
            questionSessionRepository.save(sessionDoc);

            throw new CustomException("Session time expired", HttpStatus.BAD_REQUEST);
        }
    }

    
    public VerifiedTimestampResult verifyTimestamp(OffsetDateTime frontendTime) {
        if (frontendTime == null) {
            throw new CustomException("Invalid frontend timestamp format", HttpStatus.BAD_REQUEST);
        }

        OffsetDateTime normalizedFrontend = frontendTime.withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime backendTime = OffsetDateTime.now(ZoneOffset.UTC);

        long timeDiffSeconds = Math.abs(Duration.between(backendTime, normalizedFrontend).getSeconds());

        if (timeDiffSeconds > 120) {
            throw new CustomException("Submission time mismatch exceeds 2 minutes", HttpStatus.BAD_REQUEST);
        }

        return VerifiedTimestampResult.builder()
                .frontendTime(normalizedFrontend)
                .backendTime(backendTime)
                .build();
    }

    private UUID parseUuid(String idStr, String errorMessage, HttpStatus status) {
        try {
            return UUID.fromString(idStr);
        } catch (Exception e) {
            throw new CustomException(errorMessage, status);
        }
    }
}