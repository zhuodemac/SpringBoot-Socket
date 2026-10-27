FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY settings.xml /root/.m2/settings.xml

RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/socket-chat-backend.jar app.jar

EXPOSE 8080 19090

ENTRYPOINT ["java", "-jar", "app.jar"]