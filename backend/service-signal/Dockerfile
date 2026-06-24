FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/service-signal-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 9085

ENTRYPOINT ["java", "-jar", "app.jar"]
