# -----------------------------------------------------------------------------
# STAGE 1: Build the JAR (The Gradle Builder)
# -----------------------------------------------------------------------------
FROM gradle:jdk21-alpine AS builder

WORKDIR /app

# 1. Copy dependency definitions first (Caching layer)
COPY build.gradle settings.gradle ./
# Copy gradle wrapper files
COPY gradlew ./
COPY gradle ./gradle

# Fix execution permissions for gradlew just in case
RUN chmod +x gradlew

# 2. Download dependencies (This layer will be cached if dependencies don't change)
# We use 'no-daemon' because Docker containers don't need long-running Gradle daemons
RUN ./gradlew dependencies --no-daemon

# 3. Copy Source Code
COPY src ./src

# 4. Build the executable JAR
# 'bootJar' is specific to Spring Boot, creates the runnable JAR in build/libs/
RUN ./gradlew clean bootJar --no-daemon -x test

# -----------------------------------------------------------------------------
# STAGE 2: Runtime (The Lightweight Image)
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from the 'builder' stage
# We assume only one JAR is generated in libs/
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose common ports (You can ignore specific ones per app, this just documents them)
EXPOSE 8083 8084

# Run it
ENTRYPOINT ["java", "-jar", "app.jar"]