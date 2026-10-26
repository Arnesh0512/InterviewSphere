package com.arnesh.interviewsphere.verification;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.arnesh.interviewsphere.constant.Skill;
import com.arnesh.interviewsphere.dto.request.ContestCreateRequest;
import com.arnesh.interviewsphere.entity.Contest;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.ContestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContestValidationService {

    private final ContestRepository contestRepository;

    
    public boolean verifyDuplicateContest(ContestCreateRequest data) {
        List<Skill> sortedSkills = data.getSkills() != null 
                ? data.getSkills().stream().sorted().toList() 
                : Collections.emptyList();
        Collections.sort(sortedSkills);

        boolean exists = contestRepository.existsMatchingContest(
                data.getCompany(),
                data.getRole(),
                sortedSkills,
                data.getCandidateCapacity(),
                data.getLastDateToRegister(),
                data.getContestStart(),
                data.getResumeRound().getStart(),
                data.getCodingRound().getStart(),
                data.getConceptRound().getStart(),
                data.getHrRound().getStart());

        if (exists) {
            throw new CustomException("Contest with same configuration already exists", HttpStatus.CONFLICT);
        }

        return true;
    }

    
    public void validateContestData(ContestCreateRequest data) {
        OffsetDateTime nowWithBuffer = OffsetDateTime.now().plusHours(1);

        OffsetDateTime reg = data.getLastDateToRegister();
        OffsetDateTime cStart = data.getContestStart();
        OffsetDateTime cEnd = data.getContestEnd();
        OffsetDateTime lBoard = data.getLeaderboardDeclareTime();

        OffsetDateTime resStart = data.getResumeRound().getStart();
        OffsetDateTime resEnd = data.getResumeRound().getEnd();
        int resDuration = data.getResumeRound().getDuration();

        OffsetDateTime codStart = data.getCodingRound().getStart();
        OffsetDateTime codEnd = data.getCodingRound().getEnd();
        int codDuration = data.getCodingRound().getDuration();

        OffsetDateTime conStart = data.getConceptRound().getStart();
        OffsetDateTime conEnd = data.getConceptRound().getEnd();
        int conDuration = data.getConceptRound().getDuration();

        OffsetDateTime hrStart = data.getHrRound().getStart();
        OffsetDateTime hrEnd = data.getHrRound().getEnd();
        int hrDuration = data.getHrRound().getDuration();

        // 1. Validate Time Sequence
        boolean timeSeqValid = nowWithBuffer.isBefore(reg)
                && reg.isBefore(resStart) && resStart.isBefore(resEnd)
                && cStart.isBefore(data.getResumeRound().getResult())
                && codStart.isBefore(codEnd) && codEnd.isBefore(data.getCodingRound().getResult())
                && conStart.isBefore(conEnd) && conEnd.isBefore(data.getConceptRound().getResult())
                && hrStart.isBefore(hrEnd) && hrEnd.isBefore(data.getHrRound().getResult())
                && cEnd.isBefore(lBoard);

        if (!timeSeqValid) {
            throw new CustomException("Time sequence invalid", HttpStatus.BAD_REQUEST);
        }

        // 2. Validate Selection Capacity Hierarchy
        boolean selectionValid = data.getSelectedHr() <= data.getSelectedConcept()
                && data.getSelectedConcept() <= data.getSelectedCoding()
                && data.getSelectedCoding() <= data.getSelectedResume()
                && data.getSelectedResume() <= data.getCandidateCapacity();

        if (!selectionValid) {
            throw new CustomException("Selection sequence invalid", HttpStatus.BAD_REQUEST);
        }

        if (Duration.between(resStart, resEnd).getSeconds() < resDuration) {
            throw new CustomException("Resume duration invalid", HttpStatus.BAD_REQUEST);
        }
        if (Duration.between(codStart, codEnd).getSeconds() < codDuration) {
            throw new CustomException("Coding duration invalid", HttpStatus.BAD_REQUEST);
        }
        if (Duration.between(conStart, conEnd).getSeconds() < conDuration) {
            throw new CustomException("Concept duration invalid", HttpStatus.BAD_REQUEST);
        }
        if (Duration.between(hrStart, hrEnd).getSeconds() < hrDuration) {
            throw new CustomException("HR duration invalid", HttpStatus.BAD_REQUEST);
        }

        if (data.getResumeQuestionsCount() < 1
                || data.getCodingQuestionsCount() < 1
                || data.getConceptQuestionsCount() < 1
                || data.getHrQuestionsCount() < 2) {
            throw new CustomException("Error in number of questions", HttpStatus.BAD_REQUEST);
        }

        if ((resDuration / 60.0) < data.getResumeQuestionsCount()
                || (codDuration / 60.0) < data.getCodingQuestionsCount()
                || (conDuration / 60.0) < data.getConceptQuestionsCount()
                || (hrDuration / 60.0) < data.getHrQuestionsCount()) {
            throw new CustomException("Not enough time provided.", HttpStatus.BAD_REQUEST);
        }
    }

    
    public Contest verifyContestId(String contestIdStr) {
        UUID contestId;
        try {
            contestId = UUID.fromString(contestIdStr);
        } catch (Exception e) {
            throw new CustomException("Invalid contest_id", HttpStatus.BAD_REQUEST);
        }

        return contestRepository.findById(contestId)
                .orElseThrow(() -> new CustomException("Contest not found", HttpStatus.NOT_FOUND));
    }
}