# Multi-stage Dockerfile for applications/app-service
# Builder: uses Gradle (with JDK 21) to produce the Spring Boot fat JAR
# Runtime: lightweight Eclipse Temurin JRE 21

FROM gradle:8.6-jdk21 AS builder
WORKDIR /home/gradle/project

# Copy the whole project and build the module's bootJar (skip tests for speed)
COPY --chown=gradle:gradle . .
RUN gradle :app-service:bootJar -x test --no-daemon

# Collect the built jar into a known path
RUN mkdir -p /build-output \
 && cp applications/app-service/build/libs/*.jar /build-output/app.jar

FROM eclipse-temurin:21-jre AS runtime
LABEL maintainer="team"
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"
EXPOSE 8081
VOLUME /tmp

COPY --from=builder /build-output/app.jar /app/app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
