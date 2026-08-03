package com.arnesh.interviewsphere.service;

import com.arnesh.interviewsphere.dto.request.UserCreateRequest;
import com.arnesh.interviewsphere.entity.Candidate;
import com.arnesh.interviewsphere.entity.CandidateEducation;
import com.arnesh.interviewsphere.exception.CustomException;
import com.arnesh.interviewsphere.repository.CandidateRepository;
import com.arnesh.interviewsphere.security.UserPrincipal;
import com.arnesh.interviewsphere.verification.CandidateVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final CandidateVerificationService candidateVerificationService;

    @Transactional
    public Map<String, Object> registerCandidate(UserPrincipal principal, UserCreateRequest request) {
        var verified = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = verified.getCandidate();

        if (candidate.getFullName() != null && !candidate.getFullName().isBlank()) {
            throw new CustomException("Candidate Already Registered", HttpStatus.NOT_FOUND);
        }

        if (!principal.getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new CustomException("Email mismatch", HttpStatus.BAD_REQUEST);
        }

        applyUpdateData(candidate, request);
        candidate.setTotalResumes(0);
        candidate.setTotalGithubs(0);
        candidate.setTotalCodings(0);
        candidate.setTotalConcepts(0);

        candidateRepository.save(candidate);

        return Map.of(
                "success", true,
                "message", "Candidate registered successfully");
    }

    @Transactional
    public Map<String, Object> changeCandidateDetails(UserPrincipal principal, UserCreateRequest request) {
        var verified = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = verified.getCandidate();

        if (!principal.getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new CustomException("Email mismatch", HttpStatus.BAD_REQUEST);
        }

        applyUpdateData(candidate, request);
        candidateRepository.save(candidate);

        return Map.of(
                "success", true,
                "message", "Candidate details updated successfully");
    }

    public Map<String, Object> getCandidateProfile(UserPrincipal principal) {
        var verified = candidateVerificationService.verifyCandidateById(principal.getId().toString(), "Y");
        Candidate candidate = verified.getCandidate();

        return Map.of(
                "success", true,
                "data", candidate);
    }

    private void applyUpdateData(Candidate candidate, UserCreateRequest request) {
        candidate.setFullName(request.getFullName());
        candidate.setPhoneNumber(request.getPhoneNumber());
        candidate.setGender(request.getGender());
        candidate.setDateOfBirth(request.getDateOfBirth());
        candidate.setLinkedinUrl(request.getLinkedinUrl());
        candidate.setGithubUrl(request.getGithubUrl());
        candidate.setRoles(request.getRoles());
        candidate.setSkills(request.getSkills());

        if (request.getEducation() != null) {
            candidate.getEducation().clear();
            for (var edu : request.getEducation()) {
                CandidateEducation educationEntity = CandidateEducation.builder()
                        .institutionName(edu.getInstitutionName())
                        .degree(edu.getDegree())
                        .fieldOfStudy(edu.getFieldOfStudy())
                        .gradeCgpa(edu.getGradeCgpa())
                        .startDate(edu.getStartDate())
                        .endDate(edu.getEndDate())
                        .isCurrent(edu.getIsCurrent())
                        .build();
                candidate.addEducation(educationEntity);
            }
        }
    }
}