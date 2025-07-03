# Use a build stage to compile the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /home/app
COPY . .
RUN mvn clean package -DskipTests

# Use a lightweight JDK runtime for the final image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
