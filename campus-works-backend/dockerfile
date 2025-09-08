# Step 1: Use Maven image to build the JAR
FROM maven:3.9.4-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (better cache usage)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Step 2: Use smaller JDK image to run the app
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 for Render
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
