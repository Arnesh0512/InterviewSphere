package com.arnesh.interviewsphere.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Skill {

    // Programming Languages
    C("C"),
    CPP("Cpp"),
    JAVA("Java"),
    PYTHON("Python"),
    JAVASCRIPT("JavaScript"),
    TYPESCRIPT("TypeScript"),
    GO("Go"),
    RUST("Rust"),
    KOTLIN("Kotlin"),
    SWIFT("Swift"),
    PHP("PHP"),
    CSHARP("C#"),
    R("R"),
    DART("Dart"),
    SCALA("Scala"),
    BASH("Bash"),
    ASSEMBLY("Assembly"),
    MATLAB("MATLAB"),
    GROOVY("Groovy"),
    HASKELL("Haskell"),
    ELIXIR("Elixir"),
    JULIA("Julia"),
    COBOL("COBOL"),
    FORTRAN("Fortran"),
    SOLIDITY("Solidity"),
    VBA("VBA"),
    OBJECTIVE_C("Objective-C"),
    POWERSHELL("PowerShell"),
    SCRATCH("Scratch"),
    CRYSTAL("Crystal"),
    NIM("Nim"),
    ADA("Ada"),
    GDSCRIPT("GDScript"),
    FSHARP("F#"),
    PERL("Perl"),
    SHELL_SCRIPTING("Shell Scripting"),
    WEBASSEMBLY("WebAssembly"),
    PLSQL("PL/SQL"),
    ABAP("ABAP"),
    HACK_HHVM("Hack (HHVM)"),
    ZIG("Zig"),
    VERILOG("Verilog"),
    VHDL("VHDL"),

    // Frontend
    HTML("HTML"),
    CSS("CSS"),
    REACT("React"),
    NEXTJS("Next.js"),
    ANGULAR("Angular"),
    VUEJS("Vue.js"),
    TAILWIND_CSS("Tailwind CSS"),
    BOOTSTRAP("Bootstrap"),

    // Backend
    NODEJS("Node.js"),
    EXPRESSJS("Express.js"),
    DJANGO("Django"),
    FLASK("Flask"),
    FASTAPI("FastAPI"),
    SPRING_BOOT("Spring Boot"),
    ASP_NET("ASP.NET"),

    // Databases
    MYSQL("MySQL"),
    POSTGRESQL("PostgreSQL"),
    MONGODB("MongoDB"),
    REDIS("Redis"),
    SQLITE("SQLite"),
    ORACLE_DB("Oracle DB"),
    FIREBASE("Firebase"),
    CASSANDRA("Cassandra"),

    // DevOps / Cloud
    DOCKER("Docker"),
    KUBERNETES("Kubernetes"),
    AWS("AWS"),
    AZURE("Azure"),
    GOOGLE_CLOUD("Google Cloud"),
    CICD("CI/CD"),
    TERRAFORM("Terraform"),
    NGINX("Nginx"),

    // AI / ML
    TENSORFLOW("TensorFlow"),
    PYTORCH("PyTorch"),
    SCIKIT_LEARN("Scikit-learn"),
    OPENCV("OpenCV"),
    PANDAS("Pandas"),
    NUMPY("NumPy"),
    LANGCHAIN("LangChain"),
    HUGGINGFACE("HuggingFace"),
    LLMS("LLMs"),
    RAG("RAG"),

    // Tools
    GIT("Git"),
    GITHUB("GitHub"),
    GITLAB("GitLab"),
    LINUX("Linux"),
    POSTMAN("Postman"),
    JIRA("Jira"),

    // Other
    REST_API("REST API"),
    GRAPHQL("GraphQL"),
    MICROSERVICES("Microservices"),
    SYSTEM_DESIGN("System Design"),
    DATA_STRUCTURES("Data Structures"),
    ALGORITHMS("Algorithms");

    @JsonValue
    private final String value;

    Skill(String value) {
        this.value = value;
    }

    public static Skill fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Skill value cannot be null");
        }
        for (Skill skill : Skill.values()) {
            if (skill.value.equalsIgnoreCase(value) || skill.name().equalsIgnoreCase(value)) {
                return skill;
            }
        }
        throw new IllegalArgumentException("Unknown skill value: " + value);
    }
}