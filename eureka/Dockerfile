# Stage 1: Build the application
FROM eclipse-temurin:17-jdk AS builder

# Set working directory
WORKDIR /build

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build the application (skip tests for faster builds)
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Expose port
EXPOSE 8761

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

