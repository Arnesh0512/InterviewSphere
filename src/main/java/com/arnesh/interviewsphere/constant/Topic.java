package com.arnesh.interviewsphere.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Topic {

    // Programming Languages
    C_PROGRAMMING("C Programming"),
    CPP_PROGRAMMING("C++ Programming"),
    JAVA_PROGRAMMING("Java Programming"),
    PYTHON_PROGRAMMING("Python Programming"),
    JAVASCRIPT_PROGRAMMING("JavaScript Programming"),
    TYPESCRIPT_PROGRAMMING("TypeScript Programming"),
    GO_PROGRAMMING("Go Programming"),
    RUST_PROGRAMMING("Rust Programming"),
    KOTLIN_PROGRAMMING("Kotlin Programming"),
    SWIFT_PROGRAMMING("Swift Programming"),
    PHP_PROGRAMMING("PHP Programming"),
    CSHARP_PROGRAMMING("C# Programming"),
    R_PROGRAMMING("R Programming"),
    DART_PROGRAMMING("Dart Programming"),
    SCALA_PROGRAMMING("Scala Programming"),
    BASH_SCRIPTING("Bash Scripting"),
    ASSEMBLY_PROGRAMMING("Assembly Programming"),
    MATLAB_PROGRAMMING("MATLAB Programming"),
    GROOVY_PROGRAMMING("Groovy Programming"),
    HASKELL_PROGRAMMING("Haskell Programming"),
    ELIXIR_PROGRAMMING("Elixir Programming"),
    JULIA_PROGRAMMING("Julia Programming"),
    COBOL_PROGRAMMING("COBOL Programming"),
    FORTRAN_PROGRAMMING("Fortran Programming"),
    SOLIDITY_PROGRAMMING("Solidity Programming"),
    VBA_PROGRAMMING("VBA Programming"),
    OBJECTIVE_C_PROGRAMMING("Objective-C Programming"),
    POWERSHELL_SCRIPTING("PowerShell Scripting"),
    SCRATCH_PROGRAMMING("Scratch Programming"),
    CRYSTAL_PROGRAMMING("Crystal Programming"),
    NIM_PROGRAMMING("Nim Programming"),
    ADA_PROGRAMMING("Ada Programming"),
    GDSCRIPT_PROGRAMMING("GDScript Programming"),
    FSHARP_PROGRAMMING("F# Programming"),
    PERL_PROGRAMMING("Perl Programming"),
    SHELL_SCRIPTING("Shell Scripting"),
    WEBASSEMBLY_PROGRAMMING("WebAssembly Programming"),
    PLSQL_PROGRAMMING("PL/SQL Programming"),
    ABAP_PROGRAMMING("ABAP Programming"),
    HACK_HHVM_PROGRAMMING("Hack (HHVM) Programming"),
    ZIG_PROGRAMMING("Zig Programming"),
    VERILOG_PROGRAMMING("Verilog Programming"),
    VHDL_PROGRAMMING("VHDL Programming"),

    // Frontend Development
    HTML_DEVELOPMENT("HTML Development"),
    CSS_DEVELOPMENT("CSS Development"),
    REACT_DEVELOPMENT("React Development"),
    NEXTJS_DEVELOPMENT("Next.js Development"),
    ANGULAR_DEVELOPMENT("Angular Development"),
    VUEJS_DEVELOPMENT("Vue.js Development"),
    TAILWIND_CSS_DEVELOPMENT("Tailwind CSS Development"),
    BOOTSTRAP_FRAMEWORK("Bootstrap Framework"),

    // Backend Development
    NODEJS_DEVELOPMENT("Node.js Development"),
    EXPRESSJS_DEVELOPMENT("Express.js Development"),
    DJANGO_DEVELOPMENT("Django Development"),
    FLASK_DEVELOPMENT("Flask Development"),
    FASTAPI_DEVELOPMENT("FastAPI Development"),
    SPRING_BOOT_DEVELOPMENT("Spring Boot Development"),
    ASP_NET_DEVELOPMENT("ASP.NET Development"),

    // Databases
    MYSQL_DATABASE("MySQL Database"),
    POSTGRESQL_DATABASE("PostgreSQL Database"),
    MONGODB_DATABASE("MongoDB Database"),
    REDIS_DATABASE("Redis Database"),
    SQLITE_DATABASE("SQLite Database"),
    ORACLE_DATABASE("Oracle Database"),
    FIREBASE_DEVELOPMENT("Firebase Development"),
    CASSANDRA_DATABASE("Cassandra Database"),

    // DevOps / Cloud / Infrastructure
    DOCKER_CONTAINERIZATION("Docker Containerization"),
    KUBERNETES_ORCHESTRATION("Kubernetes Orchestration"),
    AMAZON_WEB_SERVICES("Amazon Web Services"),
    MICROSOFT_AZURE("Microsoft Azure"),
    GOOGLE_CLOUD_PLATFORM("Google Cloud Platform"),
    CICD_PIPELINE("CI/CD Pipeline"),
    TERRAFORM_INFRASTRUCTURE("Terraform Infrastructure"),
    NGINX_SERVER("Nginx Server"),

    // AI / ML / Data Libraries
    TENSORFLOW_FRAMEWORK("TensorFlow Framework"),
    PYTORCH_FRAMEWORK("PyTorch Framework"),
    SCIKIT_LEARN_LIBRARY("Scikit-learn Library"),
    OPENCV_LIBRARY("OpenCV Library"),
    PANDAS_LIBRARY("Pandas Library"),
    NUMPY_LIBRARY("NumPy Library"),
    LANGCHAIN_FRAMEWORK("LangChain Framework"),
    HUGGINGFACE_TRANSFORMERS("HuggingFace Transformers"),
    LARGE_LANGUAGE_MODELS("Large Language Models"),
    RETRIEVAL_AUGMENTED_GENERATION("Retrieval Augmented Generation"),

    // Tools & Version Control
    GIT_VERSION_CONTROL("Git Version Control"),
    GITHUB_PLATFORM("GitHub Platform"),
    GITLAB_PLATFORM("GitLab Platform"),
    LINUX_OPERATING_SYSTEM("Linux Operating System"),
    POSTMAN_API_TESTING("Postman API Testing"),
    JIRA_PROJECT_MANAGEMENT("Jira Project Management"),

    // Core CS & Software Development
    REST_API_DEVELOPMENT("REST API Development"),
    GRAPHQL_API_DEVELOPMENT("GraphQL API Development"),
    MICROSERVICES_ARCHITECTURE("Microservices Architecture"),
    SYSTEM_DESIGN("System Design"),
    DATA_STRUCTURES("Data Structures"),
    ALGORITHMS("Algorithms"),
    OBJECT_ORIENTED_PROGRAMMING("Object Oriented Programming"),

    OPERATING_SYSTEMS("Operating Systems"),
    DATABASE_MANAGEMENT_SYSTEMS("Database Management Systems"),
    COMPUTER_NETWORKS("Computer Networks"),
    SOFTWARE_ENGINEERING("Software Engineering"),
    COMPILER_DESIGN("Compiler Design"),
    THEORY_OF_COMPUTATION("Theory of Computation"),
    DIGITAL_LOGIC_DESIGN("Digital Logic Design"),
    COMPUTER_ORGANIZATION_AND_ARCHITECTURE("Computer Organization and Architecture"),
    MICROPROCESSORS("Microprocessors"),

    ARTIFICIAL_INTELLIGENCE("Artificial Intelligence"),
    MACHINE_LEARNING("Machine Learning"),
    DEEP_LEARNING("Deep Learning"),
    NATURAL_LANGUAGE_PROCESSING("Natural Language Processing"),
    COMPUTER_VISION("Computer Vision"),
    DISTRIBUTED_SYSTEMS("Distributed Systems"),
    CLOUD_COMPUTING("Cloud Computing"),
    PARALLEL_COMPUTING("Parallel Computing"),
    BIG_DATA("Big Data"),

    DISCRETE_MATHEMATICS("Discrete Mathematics"),
    LINEAR_ALGEBRA("Linear Algebra"),
    PROBABILITY_AND_STATISTICS("Probability and Statistics"),
    NUMERICAL_METHODS("Numerical Methods"),

    CRYPTOGRAPHY("Cryptography"),
    CYBER_SECURITY("Cyber Security"),
    INFORMATION_SECURITY("Information Security"),

    DESIGN_PATTERNS("Design Patterns"),
    SOFTWARE_TESTING("Software Testing"),
    AGILE_METHODOLOGY("Agile Methodology"),
    DEVOPS("DevOps"),

    HUMAN_COMPUTER_INTERACTION("Human Computer Interaction"),
    BLOCKCHAIN_TECHNOLOGY("Blockchain Technology"),
    INTERNET_OF_THINGS("Internet of Things"),
    MOBILE_COMPUTING("Mobile Computing"),

    // High-Level Development Domains
    FRONTEND_DEVELOPMENT("Frontend Development"),
    BACKEND_DEVELOPMENT("Backend Development"),
    FULL_STACK_DEVELOPMENT("Full Stack Development"),
    WEB_DEVELOPMENT("Web Development"),
    MOBILE_APP_DEVELOPMENT("Mobile App Development"),

    // Cloud & Deployment Domains
    CLOUD_DEPLOYMENT("Cloud Deployment"),
    CLOUD_INFRASTRUCTURE("Cloud Infrastructure"),
    DEVOPS_PRACTICES("DevOps Practices"),
    SITE_RELIABILITY_ENGINEERING("Site Reliability Engineering"),
    PLATFORM_ENGINEERING("Platform Engineering"),

    // AI / Data Domains
    GENERATIVE_AI("Generative AI"),
    DATA_ENGINEERING("Data Engineering"),
    BIG_DATA_ENGINEERING("Big Data Engineering"),

    // Systems Domains
    EMBEDDED_SYSTEMS("Embedded Systems"),
    GAME_DEVELOPMENT("Game Development"),
    GRAPHICS_PROGRAMMING("Graphics Programming"),

    // Security Domains
    CYBERSECURITY("Cybersecurity"),
    PENETRATION_TESTING("Penetration Testing"),
    SECURITY_ENGINEERING("Security Engineering"),

    // Architecture & Leadership
    SOFTWARE_ARCHITECTURE("Software Architecture"),
    TECHNICAL_LEADERSHIP("Technical Leadership"),
    ENGINEERING_MANAGEMENT("Engineering Management"),

    // Emerging Domains
    AR_VR_DEVELOPMENT("AR/VR Development"),
    BLOCKCHAIN_DEVELOPMENT("Blockchain Development");

    @JsonValue
    private final String value;

    Topic(String value) {
        this.value = value;
    }

    public static Topic fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Topic value cannot be null");
        }
        for (Topic topic : Topic.values()) {
            if (topic.value.equalsIgnoreCase(value) || topic.name().equalsIgnoreCase(value)) {
                return topic;
            }
        }
        throw new IllegalArgumentException("Unknown topic value: " + value);
    }
}