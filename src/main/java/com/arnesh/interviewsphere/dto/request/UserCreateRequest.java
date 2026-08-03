package com.arnesh.interviewsphere.dto.request;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.validator.constraints.URL;

import com.arnesh.interviewsphere.constant.Role;
import com.arnesh.interviewsphere.constant.Skill;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
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
public class UserCreateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @Pattern(regexp = "^[MFO]$", message = "Gender must be 'M', 'F', or 'O'")
    private String gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotEmpty(message = "Roles cannot be empty")
    private List<Role> roles;

    @NotEmpty(message = "Skills cannot be empty")
    private List<Skill> skills;

    @Valid
    @NotEmpty(message = "Education list cannot be empty")
    private List<EducationDto> education;

    @NotBlank(message = "LinkedIn URL is required")
    @URL(message = "Invalid LinkedIn URL")
    private String linkedinUrl;

    @NotBlank(message = "GitHub URL is required")
    @URL(message = "Invalid GitHub URL")
    private String githubUrl;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EducationDto {
        @NotBlank(message = "Institution name is required")
        private String institutionName;

        @NotBlank(message = "Degree is required")
        private String degree;

        @NotBlank(message = "Field of study is required")
        private String fieldOfStudy;

        @NotNull(message = "Grade CGPA is required")
        private Double gradeCgpa;

        @NotNull(message = "Start date is required")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        private LocalDate endDate;

        @NotNull(message = "isCurrent boolean flag is required")
        private Boolean isCurrent;
    }
}