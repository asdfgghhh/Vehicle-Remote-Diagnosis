FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/service-diagnosis-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 9087

ENTRYPOINT ["java", "-jar", "app.jar"]
