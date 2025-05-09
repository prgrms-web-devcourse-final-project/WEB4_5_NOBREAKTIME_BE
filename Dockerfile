FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu

# 필수 패키지 설치
RUN apt-get update && \
    apt-get install -y yt-dlp ffmpeg && \
    rm -rf /var/lib/apt/lists/*

# 빌드 시점 변수 선언
ARG PROFILES=local
ARG ENV=blue

# 런타임 환경 변수 설정
ENV PROFILES=${PROFILES}
ENV ENV=${ENV}

COPY /build/libs/mallang-backend-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILES}", "-Dserver.env=${ENV}", "-jar", "/app.jar"]