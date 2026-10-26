package com.arnesh.interviewsphere.verification;

import com.arnesh.interviewsphere.entity.PracticeCodingTrack;
import com.arnesh.interviewsphere.entity.QuestionSession;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.PracticeCodingTrackRepository;
import com.arnesh.interviewsphere.repository.QuestionSessionRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CodingVerificationService {

    private final PracticeCodingTrackRepository practiceCodingTrackRepository;
    private final QuestionSessionRepository questionSessionRepository;

    @Data
    @Builder
    public static class VerifiedCodingResult {
        private PracticeCodingTrack codingTrack;
        private UUID codingId;
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

    
    public VerifiedCodingResult verifyCoding(String codingIdStr, UUID candidateId) {
        UUID codingId = parseUuid(codingIdStr, "Invalid coding_id", HttpStatus.BAD_REQUEST);

        PracticeCodingTrack track = practiceCodingTrackRepository
                .findByIdAndCandidateId(codingId, candidateId)
                .orElseThrow(() -> new CustomException("Coding document not found", HttpStatus.NOT_FOUND));

        return VerifiedCodingResult.builder()
                .codingTrack(track)
                .codingId(codingId)
                .build();
    }

    
    public void verifyQuantity(int numQuestion, PracticeCodingTrack codingTrack) {
        if (numQuestion <= 0) {
            throw new CustomException("Number of questions must be greater than 0.", HttpStatus.BAD_REQUEST);
        }

        int available = Objects.requireNonNullElse(codingTrack.getAvailableQues(), 0);

        if (numQuestion > available) {
            throw new CustomException(
                    String.format("Requested %d questions but only %d available.", numQuestion, available),
                    HttpStatus.BAD_REQUEST);
        }
    }

    
    public VerifiedSessionResult verifyQuestionSession(String questionSessionIdStr, UUID codingId) {
        UUID sessionObjId = parseUuid(questionSessionIdStr, "Invalid question_session_id", HttpStatus.BAD_REQUEST);

        QuestionSession sessionDoc = questionSessionRepository
                .findByIdAndCodingTrackId(sessionObjId, codingId)
                .orElseThrow(() -> new CustomException("Question session not found", HttpStatus.NOT_FOUND));

        return VerifiedSessionResult.builder()
                .sessionDoc(sessionDoc)
                .sessionObjId(sessionObjId)
                .build();
    }

    
    public Integer verifyQuestionId(QuestionSession sessionDoc, Integer questionId) {
        List<QuestionSession.Question> questionBank = sessionDoc.getQuestionBank() != null
                ? sessionDoc.getQuestionBank()
                : List.of();

        boolean exists = questionBank.stream()
                .map(q -> q.getQuestionId())
                .filter(Objects::nonNull)
                .anyMatch(id -> id.equals(questionId));

        if (!exists) {
            throw new CustomException("Invalid question_id", HttpStatus.BAD_REQUEST);
        }

        return questionId;
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