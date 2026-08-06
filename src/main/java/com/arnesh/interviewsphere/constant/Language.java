package com.arnesh.interviewsphere.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Language {

    PYTHON("Python"),
    JAVA("Java"),
    CPP("Cpp"),
    C("C"),
    CSHARP("C#"),
    JAVASCRIPT("JavaScript"),
    TYPESCRIPT("TypeScript"),
    GO("Go"),
    RUST("Rust"),
    KOTLIN("Kotlin"),
    SWIFT("Swift");

    @JsonValue
    private final String value;

    Language(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Language fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Language value cannot be null");
        }
        for (Language language : Language.values()) {
            if (language.value.equalsIgnoreCase(value) || language.name().equalsIgnoreCase(value)) {
                return language;
            }
        }
        throw new IllegalArgumentException("Unknown language value: " + value);
    }
}