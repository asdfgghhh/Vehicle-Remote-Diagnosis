FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/service-auth-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 9081

ENTRYPOINT ["java", "-jar", "app.jar"]
