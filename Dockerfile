# Build Stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Download dependencies first (cached layer — speeds up rebuilds)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy only the compiled JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port 8080 for the application
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]