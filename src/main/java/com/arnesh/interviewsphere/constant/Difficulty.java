package com.arnesh.interviewsphere.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Difficulty {

    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard");

    @JsonValue
    private final String value;

    Difficulty(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Difficulty fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Difficulty value cannot be null");
        }
        for (Difficulty difficulty : Difficulty.values()) {
            if (difficulty.value.equalsIgnoreCase(value) || difficulty.name().equalsIgnoreCase(value)) {
                return difficulty;
            }
        }
        throw new IllegalArgumentException("Unknown difficulty value: " + value);
    }
}