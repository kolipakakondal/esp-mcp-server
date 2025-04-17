# Stage 1: Build the JAR using Maven
FROM maven:3.9.4-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy everything to the container
COPY . .

# Build the project and create a fat JAR
RUN mvn clean package -DskipTests

# Stage 2: Run the application with JDK
FROM eclipse-temurin:21-jdk

# Create app directory
WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /app/target/esp-mcp-server.jar /app/esp-mcp-server.jar

# Expose the port your app listens on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "esp-mcp-server.jar"]
