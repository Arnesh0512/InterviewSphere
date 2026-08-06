package com.arnesh.interviewsphere.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Role {

    // Software Development
    FRONTEND_DEVELOPER("Frontend Developer"),
    BACKEND_DEVELOPER("Backend Developer"),
    FULL_STACK_DEVELOPER("Full Stack Developer"),
    SOFTWARE_ENGINEER("Software Engineer"),
    SOFTWARE_DEVELOPER("Software Developer"),
    WEB_DEVELOPER("Web Developer"),
    MOBILE_APP_DEVELOPER("Mobile App Developer"),

    // AI / ML / Data
    MACHINE_LEARNING_ENGINEER("Machine Learning Engineer"),
    DEEP_LEARNING_ENGINEER("Deep Learning Engineer"),
    DATA_SCIENTIST("Data Scientist"),
    DATA_ANALYST("Data Analyst"),
    AI_ENGINEER("AI Engineer"),
    COMPUTER_VISION_ENGINEER("Computer Vision Engineer"),
    NLP_ENGINEER("NLP Engineer"),
    GENERATIVE_AI_ENGINEER("Generative AI Engineer"),

    // Systems / Core
    SYSTEMS_ENGINEER("Systems Engineer"),
    EMBEDDED_SYSTEMS_ENGINEER("Embedded Systems Engineer"),
    OPERATING_SYSTEMS_ENGINEER("Operating Systems Engineer"),
    COMPILER_ENGINEER("Compiler Engineer"),
    GAME_DEVELOPER("Game Developer"),
    GRAPHICS_ENGINEER("Graphics Engineer"),

    // Cloud / DevOps
    DEVOPS_ENGINEER("DevOps Engineer"),
    SITE_RELIABILITY_ENGINEER("Site Reliability Engineer"),
    CLOUD_ENGINEER("Cloud Engineer"),
    CLOUD_ARCHITECT("Cloud Architect"),
    PLATFORM_ENGINEER("Platform Engineer"),

    // Security
    CYBERSECURITY_ENGINEER("Cybersecurity Engineer"),
    SECURITY_ANALYST("Security Analyst"),
    PENETRATION_TESTER("Penetration Tester"),
    SECURITY_RESEARCHER("Security Researcher"),

    // Data Engineering
    DATA_ENGINEER("Data Engineer"),
    BIG_DATA_ENGINEER("Big Data Engineer"),
    DATABASE_ADMINISTRATOR("Database Administrator"),

    // Testing
    QA_ENGINEER("QA Engineer"),
    AUTOMATION_TEST_ENGINEER("Automation Test Engineer"),
    SDET("SDET"),

    // Architecture / Senior
    TECHNICAL_LEAD("Technical Lead"),
    ENGINEERING_MANAGER("Engineering Manager"),
    SOLUTIONS_ARCHITECT("Solutions Architect"),
    SOFTWARE_ARCHITECT("Software Architect"),

    // Product / Misc
    PRODUCT_ENGINEER("Product Engineer"),
    BLOCKCHAIN_DEVELOPER("Blockchain Developer"),
    AR_VR_DEVELOPER("AR/VR Developer"),
    IOT_ENGINEER("IoT Engineer");

    @JsonValue
    private final String value;

    Role(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Role fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Role value cannot be null");
        }
        for (Role role : Role.values()) {
            if (role.value.equalsIgnoreCase(value) || role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role value: " + value);
    }
}