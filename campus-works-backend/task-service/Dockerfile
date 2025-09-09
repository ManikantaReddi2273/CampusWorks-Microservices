# =========================
# Multi-stage build for Task Service
# =========================

# Stage 1: Build the application
FROM openjdk:17-jdk-slim as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN mvn clean package -DskipTests

# Stage 2: Create runtime image
FROM openjdk:17-jre-slim

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9001

ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVICE_PORT=9001
ENV SERVICE_NAME=task-service

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:9001/actuator/health || exit 1

CMD ["java", "-jar", "app.jar"]
