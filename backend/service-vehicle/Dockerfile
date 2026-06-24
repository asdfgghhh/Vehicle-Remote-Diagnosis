FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/service-vehicle-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 9082

ENTRYPOINT ["java", "-jar", "app.jar"]
