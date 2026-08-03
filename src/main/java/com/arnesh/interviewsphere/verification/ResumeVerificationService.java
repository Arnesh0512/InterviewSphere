package com.arnesh.interviewsphere.verification;

import com.arnesh.interviewsphere.entity.PracticeResumeTrack;
import com.arnesh.interviewsphere.entity.QuestionSession;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.PracticeResumeTrackRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeVerificationService {

    private final PracticeResumeTrackRepository practiceResumeTrackRepository;
    private final QuestionSessionRepository questionSessionRepository;

    @Data
    @Builder
    public static class VerifiedResumeResult {
        private PracticeResumeTrack resumeTrack;
        private UUID resumeId;
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

    /**
     * Verifies existence and ownership of a resume track for a candidate.
     */
    public VerifiedResumeResult verifyResume(String resumeIdStr, UUID candidateId) {
        UUID resumeId = parseUuid(resumeIdStr, "Invalid resume_id", HttpStatus.BAD_REQUEST);

        PracticeResumeTrack track = practiceResumeTrackRepository
                .findByIdAndCandidateId(resumeId, candidateId)
                .orElseThrow(() -> new CustomException("Resume document not found", HttpStatus.NOT_FOUND));

        return VerifiedResumeResult.builder()
                .resumeTrack(track)
                .resumeId(resumeId)
                .build();
    }

    /**
     * Verifies existence of a question session belonging to a resume track.
     */
    public VerifiedSessionResult verifyQuestionSession(String questionSessionIdStr, UUID resumeId) {
        UUID sessionObjId = parseUuid(questionSessionIdStr, "Invalid question_session_id", HttpStatus.BAD_REQUEST);

        QuestionSession sessionDoc = questionSessionRepository
                .findByIdAndResumeTrackId(sessionObjId, resumeId)
                .orElseThrow(() -> new CustomException("Question session not found", HttpStatus.NOT_FOUND));

        return VerifiedSessionResult.builder()
                .sessionDoc(sessionDoc)
                .sessionObjId(sessionObjId)
                .build();
    }

    /**
     * Verifies if a given question number is within valid bounds for the session's
     * question bank.
     */
    public void verifyQuestionId(QuestionSession sessionDoc, int questionId) {
        List<QuestionSession.Question> questionBank = sessionDoc.getQuestionBank() != null
                ? sessionDoc.getQuestionBank()
                : List.of();

        if (questionId < 1 || questionId > questionBank.size()) {
            throw new CustomException("Invalid question_id", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Verifies that the question session status is 'active'.
     */
    public void verifySessionStatus(QuestionSession sessionDoc) {
        if (!"active".equalsIgnoreCase(sessionDoc.getStatus())) {
            throw new CustomException("Session is not active", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Verifies that the question session status is 'passive'.
     */
    public void verifySessionStatus2(QuestionSession sessionDoc) {
        if (!"passive".equalsIgnoreCase(sessionDoc.getStatus())) {
            throw new CustomException("Session is not passive", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Verifies whether session time has expired. If expired, updates status to
     * 'passive'.
     */
    public void verifySessionTime(QuestionSession sessionDoc, UUID sessionObjId) {
        OffsetDateTime startTime = sessionDoc.getTimestamp().withOffsetSameInstant(ZoneOffset.UTC);
        int totalTime = sessionDoc.getTime() != null ? sessionDoc.getTime() : 0;

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        double elapsedMinutes = Duration.between(startTime, now).toSeconds() / 60.0;

        if (elapsedMinutes > totalTime) {
            sessionDoc.setStatus("passive");
            questionSessionRepository.save(sessionDoc);

            throw new CustomException("Session time expired", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Verifies that frontend submission time does not drift more than 2 minutes
     * from backend time.
     */
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

    /**
     * Verifies that a given file ID / storage key is valid.
     */
    public String verifyFileId(String fileIdStr) {
        if (fileIdStr == null || fileIdStr.isBlank()) {
            throw new CustomException("Invalid file_id", HttpStatus.BAD_REQUEST);
        }
        return fileIdStr;
    }

    private UUID parseUuid(String idStr, String errorMessage, HttpStatus status) {
        try {
            return UUID.fromString(idStr);
        } catch (Exception e) {
            throw new CustomException(errorMessage, status);
        }
    }
}