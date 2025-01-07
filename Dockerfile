# Stage 1: Build the application using Gradle and cache dependencies
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy only the Gradle configuration files to cache dependencies
COPY settings.gradle build.gradle ./

# Cache dependencies without building the entire project
RUN gradle dependencies --no-daemon

# Copy the full project source code
COPY . .

# Build the application excluding tests
RUN gradle build -x test --no-daemon

# Stage 2: Create the production-ready image with a minimal JRE
FROM eclipse-temurin:17-jre

WORKDIR /app

# Expose the application port
EXPOSE 8080

# Copy the built JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
