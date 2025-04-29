FROM openjdk:21-slim

# 빌드 시점 변수 선언
ARG PROFILES=local
ARG ENV=blue

# 런타임 환경 변수 설정
ENV PROFILES=${PROFILES}
ENV ENV=${ENV}

COPY /build/libs/toolgether-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILES}", "-Dserver.env=${ENV}", "-jar", "/app.jar"]