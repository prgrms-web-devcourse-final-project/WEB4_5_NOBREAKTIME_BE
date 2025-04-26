FROM openjdk:21-slim

COPY ./build/libs/mallang-backend-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]