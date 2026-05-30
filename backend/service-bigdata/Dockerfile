FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/service-bigdata-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]
