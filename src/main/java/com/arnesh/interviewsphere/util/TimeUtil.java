package com.arnesh.interviewsphere.util;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TimeUtil {

    private TimeUtil() {
        // Private constructor to prevent instantiation
    }

    
    public static OffsetDateTime generateTimestamp() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}