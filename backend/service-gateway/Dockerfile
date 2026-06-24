FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/service-gateway-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 9080

ENTRYPOINT ["java", "-jar", "app.jar"]
