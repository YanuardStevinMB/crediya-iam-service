# ====== STAGE 1: build ======
FROM gradle:8.10.1-jdk21 AS build
WORKDIR /home/gradle/project

# Copy only gradle files first for better caching
COPY gradle/ gradle/
COPY build.gradle main.gradle gradle.properties settings.gradle gradlew gradlew.bat ./
COPY lombok.config ./

# Download dependencies (cached layer)
RUN gradle --no-daemon dependencies

# Copy source code
COPY . .

# Build the application (specifically the app-service module)
RUN gradle --no-daemon :app-service:clean :app-service:bootJar --no-build-cache

# ====== STAGE 2: runtime ======
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for health checks and create non-root user
RUN apk add --no-cache curl tzdata && \
    addgroup -S spring && \
    adduser -S spring -G spring && \
    chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Copy the built jar from the app-service module
COPY --from=build --chown=spring:spring /home/gradle/project/applications/app-service/build/libs/*.jar /app/app.jar

# Set timezone to UTC
ENV TZ=UTC
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
