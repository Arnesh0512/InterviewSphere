# InterviewSphere Backend

Spring Boot REST API for an interview-practice and contest platform. It provides candidate and admin workflows for resume, GitHub, coding, and concept interviews, plus contest rounds, evaluation, and leaderboards.

## Features

- Candidate and admin authentication with Google ID tokens and JWTs
- Candidate and admin profile registration and updates
- Resume upload, interview questions, answers, feedback, retries, and progress
- GitHub repository interview sessions and progress
- Coding and concept practice sessions, feedback, retries, and progress comparisons
- Admin-managed contests with resume, coding, concept, and HR rounds
- Contest submissions, round evaluation, and leaderboards
- Constants APIs for companies, topics, difficulties, languages, roles, skills, and tags
- Supabase Storage integration for resume and audio files
- PDF extraction and OCR support through PDFBox and Tess4J/Tesseract

## Stack

- Java 21 and Spring Boot
- Spring Web MVC, Spring Data JPA, and Spring Security
- PostgreSQL
- Supabase Storage
- Spring AI OpenAI integration
- PDFBox, Tess4J, and Tesseract
- JWT (`jjwt`) and Google API Client
- Maven Wrapper

## Project layout

```text
.
├── src/main/java/com/arnesh/interviewsphere/
│   ├── controller/    # REST endpoints
│   ├── service/       # Application workflows
│   ├── entity/        # JPA entities
│   ├── repository/    # Spring Data repositories
│   ├── security/      # Google authentication and JWT filters
│   ├── ai/            # AI question and evaluation services
│   ├── config/        # Application configuration and properties
│   └── util/          # PDF/OCR and shared utilities
├── src/main/resources/
│   └── application-render.yaml
├── Dockerfile
├── render.yaml
└── pom.xml
```

## Requirements

- JDK 21
- Docker for the Render deployment and native Tesseract OCR support
- PostgreSQL database
- Supabase project and storage buckets
- Google OAuth client ID
- OpenAI API key
- GitHub API token for GitHub interview features

## Configuration

Spring Boot reads configuration from application files and OS environment variables. It does **not** load a `.env` file by default. For local development, set environment variables in your shell or use an ignored local configuration file; do not commit credentials.

The application needs these settings:

| Environment variable | Purpose |
| --- | --- |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `SUPABASE_URL` | Supabase project URL |
| `SUPABASE_KEY` | Supabase API key used by storage integration |
| `SPRING_AI_OPENAI_API_KEY` | OpenAI API access |
| `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID` | Google client ID for Spring OAuth configuration |
| `GOOGLE_CLIENT_ID` | Google client ID used when verifying sign-in tokens |
| `GITHUB_API_KEY` | GitHub API access |
| `JWT_SECRET` | JWT signing secret |
| `JWT_ALGORITHM` | JWT signing algorithm, normally `HS256` |
| `JWT_EXPIRATION_MS` | JWT lifetime in milliseconds |
| `CORS_ALLOWED_ORIGINS` | Frontend origin allowed by CORS |

The Supabase bucket names default to `resumes`, `contest-resumes`, `contest-audio`, and `audio`. The Render Blueprint generates `JWT_SECRET` and sets the port profile for the hosted service.

## Run locally

From the repository root in PowerShell:

```powershell
java -version
.\mvnw.cmd spring-boot:run
```

To build a runnable JAR without running the test suite:

```powershell
.\mvnw.cmd -DskipTests package
java -jar target\interviewsphere-0.0.1-SNAPSHOT.jar
```

Open Swagger UI at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html). The OpenAPI document is at `/v3/api-docs`.

## API overview

Most application routes require a JWT in `Authorization: Bearer <JWT>`. Authentication and Swagger/OpenAPI routes are public; the `/api/dev` routes are development helpers.

| Prefix | Purpose |
| --- | --- |
| `/api/auth` | Candidate and admin Google authentication |
| `/api/candidate` | Candidate profile registration, updates, and profile retrieval |
| `/api/admin` | Admin profiles, contest management, evaluation, and results |
| `/api/resume` | Resume uploads, interview sessions, feedback, and progress |
| `/api/github` | GitHub repository interviews, feedback, and progress |
| `/api/coding` | Coding practice questions, answers, retries, and progress |
| `/api/concept` | Concept practice questions, answers, retries, and progress |
| `/api/contest` | Contest discovery, registration, round submissions, and leaderboards |
| `/api/constants` | Shared selector values |
| `/api/dev` | Development token-generation helpers |

## Deploy to Render

The root `render.yaml` defines the `interviewsphere-api` Docker web service. In Render, create a Blueprint from this repository and use the repository root (`.`). The Dockerfile builds with Java 21, installs Tesseract for OCR, and starts the Spring Boot JAR. The Render profile reads the platform `PORT`; `/v3/api-docs` is configured as the health check.

On first Blueprint setup, enter the database, Supabase, OpenAI, Google, GitHub, and CORS values listed above when prompted. The Google client ID is used in both Google variables. Set `CORS_ALLOWED_ORIGINS` to the exact frontend origin. The service is configured for the Singapore region and Render’s free plan; you can change the plan in Render if the app needs more memory or sustained availability.

## Security notes

- The `/api/dev/generate-candidate-token` and `/api/dev/generate-admin-token` endpoints issue tokens from email input and are intended for development. Disable or protect them before exposing a production service.
- Keep database credentials, Supabase keys, API keys, and JWT secrets in environment settings, not source control. Local `application.properties` files are ignored by Git.
- Protected endpoints use stateless bearer-token authentication.
