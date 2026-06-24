FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/service-dbc-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 9084

ENTRYPOINT ["java", "-jar", "app.jar"]
