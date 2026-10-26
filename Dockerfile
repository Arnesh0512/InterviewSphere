# --- Stage 1: Build the Application ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy dependency configuration files first to cache downloaded layers
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy application source code and package the fat jar
COPY src src
RUN ./mvnw clean package -DskipTests

# --- Stage 2: Minimal Runtime Image ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Tess4J needs the native Tesseract library and English trained data at runtime.
RUN apt-get update \
    && apt-get install -y --no-install-recommends tesseract-ocr \
    && rm -rf /var/lib/apt/lists/*

# Copy the compiled artifact from the build stage
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
