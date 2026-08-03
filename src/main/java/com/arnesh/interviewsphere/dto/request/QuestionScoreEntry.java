package com.arnesh.interviewsphere.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionScoreEntry {
    private UUID candidateId;
    private double rawScore;
    private OffsetDateTime submittedAt;
}