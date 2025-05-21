# 빌드 스테이지
FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /workspace
COPY . .
RUN ./gradlew clean bootJar

# 런타임 스테이지
FROM eclipse-temurin:21-jre-jammy

# 시스템 패키지 설치
RUN apt-get update && \
apt-get install -y python3 curl && \
curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp && \
chmod a+rx /usr/local/bin/yt-dlp && \
apt-get clean && \
rm -rf /var/lib/apt/lists/*

# 빌드 시점 변수 선언
ARG PROFILES=local
ARG ENV=blue

# 런타임 환경 변수 설정
ENV PROFILES=${PROFILES}
ENV ENV=${ENV}

COPY /build/libs/mallang-backend-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILES}", "-Dserver.env=${ENV}", "-jar", "/app.jar"]
