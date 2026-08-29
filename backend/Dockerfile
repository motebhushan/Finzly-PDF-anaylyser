# Build stage
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/pdf-analyser-0.0.1-SNAPSHOT.jar app.jar

# Render injects the PORT environment variable, Spring Boot uses server.port
ENV PORT=8080
EXPOSE ${PORT}

ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]
