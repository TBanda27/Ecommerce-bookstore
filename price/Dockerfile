# Use the official OpenJDK image for a slim JRE
FROM eclipse-temurin:17-jre

# Set the argument for the JAR file name
ARG JAR_FILE=price-0.0.1-SNAPSHOT.jar

# Set the container's working directory
WORKDIR /app

# Copy the built JAR file from the Maven 'target' directory into the container
COPY target/${JAR_FILE} app.jar

# Run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]

