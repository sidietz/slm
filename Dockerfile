# Base runtime image matching Java 27
FROM eclipse-temurin:27-jre

WORKDIR /app

# Accept jar file as build argument, defaulting to the current release
ARG JAR_FILE=build/libs/slm-0.0.2f.jar

# Copy the built Spring Boot application JAR into the Docker image
COPY ${JAR_FILE} application.jar

EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-Xmx2048M", "-jar", "/app/application.jar"]