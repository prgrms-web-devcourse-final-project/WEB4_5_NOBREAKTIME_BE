# --- Build stage ---
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace
COPY . .
RUN ./gradlew clean bootJar

# 빌드 시점 변수 선언
ARG PROFILES=local
ARG ENV=blue

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-jammy

# 런타임 시점 변수
ENV PROFILES=${PROFILES}
ENV ENV=${ENV}
ENV YT_EMAIL=${YT_EMAIL}
ENV YT_PASSWORD=${YT_PASSWORD}
ENV YT_COOKIES_PATH=${YT_COOKIES_PATH}

# 시스템 패키지 및 헤드리스 크롬, Python/Selenium, yt-dlp(nightly) 설치
RUN apt-get update && \
    apt-get install -y \
      wget \
      python3 python3-pip \
      curl ffmpeg && \
    wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt-get install -y ./google-chrome-stable_current_amd64.deb && \
    rm google-chrome-stable_current_amd64.deb && \
    pip3 install selenium webdriver-manager && \
    curl -L https://github.com/yt-dlp/yt-dlp-master-builds/releases/latest/download/yt-dlp \
         -o /usr/local/bin/yt-dlp && \
    chmod a+rx /usr/local/bin/yt-dlp && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 쿠키 생성 스크립트 및 엔트리포인트 복사
COPY save_headless_cookies.py /scripts/save_headless_cookies.py
COPY entrypoint.sh             /entrypoint.sh
RUN chmod +x /scripts/save_headless_cookies.py /entrypoint.sh

# Spring Boot JAR 복사
COPY --from=builder /workspace/build/libs/mallang-backend-0.0.1-SNAPSHOT.jar /app.jar

# 컨테이너 시작 시 스크립트 실행
ENTRYPOINT ["/entrypoint.sh"]