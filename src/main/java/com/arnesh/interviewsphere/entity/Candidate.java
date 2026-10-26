package com.arnesh.interviewsphere.entity;

import com.arnesh.interviewsphere.constant.Role;
import com.arnesh.interviewsphere.constant.Skill;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "profile_pic")
    private String profilePic;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(length = 10)
    private String gender; // "M", "F", "O"

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "candidate_roles", joinColumns = @JoinColumn(name = "candidate_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<Role> roles = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "candidate_skills", joinColumns = @JoinColumn(name = "candidate_id"))
    @Column(name = "skill")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CandidateEducation> education = new ArrayList<>();

    @Column(name = "total_codings")
    @Builder.Default
    private Integer totalCodings = 0;

    @Column(name = "total_concepts")
    @Builder.Default
    private Integer totalConcepts = 0;

    @Column(name = "total_githubs")
    @Builder.Default
    private Integer totalGithubs = 0;

    @Column(name = "total_resumes")
    @Builder.Default
    private Integer totalResumes = 0;

    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    private OffsetDateTime createdOn;

    public void addEducation(CandidateEducation edu) {
        education.add(edu);
        edu.setCandidate(this);
    }
}