# Stage 1: Build the application
FROM maven:3.8.8-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/jumble.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
