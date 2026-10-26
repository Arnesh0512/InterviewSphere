package com.arnesh.interviewsphere.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import com.arnesh.interviewsphere.entity.Contest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestSchedulerService {

    private final ThreadPoolTaskScheduler taskScheduler;

    
    public void runContestScheduler(
            Contest contest,
            String token,
            Consumer<UUID> generateResumeResult,
            Consumer<UUID> generateCodingResult,
            Consumer<UUID> generateConceptResult,
            Consumer<UUID> generateHrResult,
            Consumer<UUID> generateLeaderboard) {
        UUID contestId = contest.getId();

        scheduleTask(contest.getResumeRound().getResult(), () -> generateResumeResult.accept(contestId));
        scheduleTask(contest.getCodingRound().getResult(), () -> generateCodingResult.accept(contestId));
        scheduleTask(contest.getConceptRound().getResult(), () -> generateConceptResult.accept(contestId));
        scheduleTask(contest.getHrRound().getResult(), () -> generateHrResult.accept(contestId));
        scheduleTask(contest.getLeaderboardDeclareTime(), () -> generateLeaderboard.accept(contestId));
    }

    private void scheduleTask(OffsetDateTime targetTime, Runnable task) {
        OffsetDateTime now = OffsetDateTime.now();
        long delayInSeconds = Duration.between(now, targetTime).getSeconds();

        if (delayInSeconds > 0) {
            taskScheduler.schedule(task, targetTime.toInstant());
        } else {
            task.run();
        }
    }
}