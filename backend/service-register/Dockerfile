FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/service-register-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
