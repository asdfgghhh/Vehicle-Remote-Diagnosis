FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/service-ecu-log-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 9083

ENTRYPOINT ["java", "-jar", "app.jar"]
