package com.arnesh.interviewsphere.util;

import com.arnesh.interviewsphere.exception.CustomException;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DateUtils {

    private DateUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Generates a UTC timestamp corresponding to current time.
     */
    public static OffsetDateTime generateTimestamp() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Converts or enforces UTC timezone for a given timestamp.
     */
    public static OffsetDateTime toUtc(OffsetDateTime timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.withOffsetSameInstant(ZoneOffset.UTC);
    }

    /**
     * Calculates time difference in seconds between two timestamps.
     */
    public static long timeDiffInSeconds(OffsetDateTime t1, OffsetDateTime t2) {
        if (t1 == null || t2 == null) {
            return 0;
        }
        return Math.abs(Duration.between(t1, t2).getSeconds());
    }

    /**
     * Validates that frontend submission time does not drift more than 2 minutes
     * (120 seconds) from backend time.
     */
    public static OffsetDateTime verifyTimestampDrift(OffsetDateTime frontendTime) {
        if (frontendTime == null) {
            throw new CustomException("Invalid frontend timestamp format", HttpStatus.BAD_REQUEST);
        }

        OffsetDateTime utcFrontend = toUtc(frontendTime);
        OffsetDateTime utcBackend = generateTimestamp();

        if (timeDiffInSeconds(utcBackend, utcFrontend) > 120) {
            throw new CustomException("Submission time mismatch exceeds 2 minutes", HttpStatus.BAD_REQUEST);
        }

        return utcFrontend;
    }
}