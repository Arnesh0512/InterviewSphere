package com.arnesh.interviewsphere.dto.request;

import java.time.OffsetDateTime;
import java.util.List;

import com.arnesh.interviewsphere.constant.Company;
import com.arnesh.interviewsphere.constant.Role;
import com.arnesh.interviewsphere.constant.Skill;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestCreateRequest {

    @NotNull(message = "Company is required")
    private Company company;

    @NotNull(message = "Role is required")
    private Role role;

    @NotEmpty(message = "Skills cannot be empty")
    private List<Skill> skills;

    @NotNull(message = "Last date to register is required")
    private OffsetDateTime lastDateToRegister;

    @NotNull(message = "Contest start time is required")
    private OffsetDateTime contestStart;

    @NotNull(message = "Contest end time is required")
    private OffsetDateTime contestEnd;

    @Valid
    @NotNull(message = "Resume round details are required")
    private RoundTimeRequest resumeRound;

    @Valid
    @NotNull(message = "Coding round details are required")
    private RoundTimeRequest codingRound;

    @Valid
    @NotNull(message = "Concept round details are required")
    private RoundTimeRequest conceptRound;

    @Valid
    @NotNull(message = "HR round details are required")
    private RoundTimeRequest hrRound;

    @NotNull(message = "Leaderboard declare time is required")
    private OffsetDateTime leaderboardDeclareTime;

    @NotNull(message = "Resume questions count is required")
    @Min(value = 1, message = "Resume questions count must be at least 1")
    private Integer resumeQuestionsCount;

    @NotNull(message = "Coding questions count is required")
    @Min(value = 1, message = "Coding questions count must be at least 1")
    private Integer codingQuestionsCount;

    @NotNull(message = "Concept questions count is required")
    @Min(value = 1, message = "Concept questions count must be at least 1")
    private Integer conceptQuestionsCount;

    @NotNull(message = "HR questions count is required")
    @Min(value = 2, message = "HR questions count must be at least 2")
    private Integer hrQuestionsCount;

    @NotNull(message = "Candidate capacity is required")
    @Min(value = 1, message = "Candidate capacity must be at least 1")
    private Integer candidateCapacity;

    @NotNull(message = "Selected resume candidate count is required")
    @Min(value = 1, message = "Selected resume count must be at least 1")
    private Integer selectedResume;

    @NotNull(message = "Selected coding candidate count is required")
    @Min(value = 1, message = "Selected coding count must be at least 1")
    private Integer selectedCoding;

    @NotNull(message = "Selected concept candidate count is required")
    @Min(value = 1, message = "Selected concept count must be at least 1")
    private Integer selectedConcept;

    @NotNull(message = "Selected HR candidate count is required")
    @Min(value = 1, message = "Selected HR count must be at least 1")
    private Integer selectedHr;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoundTimeRequest {

        @NotNull(message = "Round start time is required")
        private OffsetDateTime start;

        @NotNull(message = "Round end time is required")
        private OffsetDateTime end;

        @NotNull(message = "Round duration is required")
        @Min(value = 1, message = "Duration must be at least 1 minute")
        private Integer duration;

        @NotNull(message = "Round result declaration time is required")
        private OffsetDateTime result;
    }
}