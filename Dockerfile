# --- Stage 1: Build the Application ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy dependency configuration files first to cache downloaded layers
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B

# Copy application source code and package the fat jar
COPY src src
RUN ./mvnw clean package -DskipTests

# --- Stage 2: Minimal Runtime Image ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the compiled artifact from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]