# syntax=docker/dockerfile:1

FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /app

# Leverage Docker layer caching
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package


FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Add a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the fat jar from the builder stage
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Expose default port; can be changed via SERVER_PORT env
EXPOSE 8080

# Default JVM options may be overridden via JAVA_OPTS
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=50 -XX:+UseContainerSupport"

# Spring config defaults; can be overridden by env or docker-compose
ENV SERVER_PORT=8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${SERVER_PORT}"]


