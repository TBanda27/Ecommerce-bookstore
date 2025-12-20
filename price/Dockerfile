# Stage 1: Build the application
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /build

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN apt-get update && apt-get install -y maven && \
    mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/target/*.jar app.jar

# Copy entrypoint script
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Expose port
EXPOSE 8200

# Run the application via entrypoint script
ENTRYPOINT ["/app/entrypoint.sh"]
