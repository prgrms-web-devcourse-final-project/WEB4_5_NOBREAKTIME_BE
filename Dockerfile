# ── 빌드 스테이지 ──
FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /workspace
COPY . .
RUN ./gradlew clean bootJar

# ── 런타임 스테이지 ──
FROM eclipse-temurin:21-jre-jammy

# 1) 시스템 패키지 + 최소 X11 라이브러리 + Chrome 설치용 도구
RUN apt-get update && \
    apt-get install -y \
      curl \
      gnupg2 \
      wget \
      python3 \
      ffmpeg \
      # X11 포워딩용 최소 라이브러리
      libgtk-3-0 \
      libx11-xcb1 \
      libxcomposite1 \
      libxdamage1 \
      libxrandr2 \
      libxss1 \
      libnss3 \
      libgbm1 \
      fonts-liberation && \
    # Google Chrome 설치
    wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub \
      | apt-key add - && \
    echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" \
      > /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && \
    apt-get install -y google-chrome-stable && \
    # yt-dlp 설치
    curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp \
      -o /usr/local/bin/yt-dlp && \
    chmod a+rx /usr/local/bin/yt-dlp && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 빌드 시점 변수 선언
ARG PROFILES=local
ARG ENV=blue

# 런타임 환경 변수 설정
ENV PROFILES=${PROFILES} \
    ENV=${ENV} \
    DISPLAY=${DISPLAY:-:0}

# 앱 JAR 복사
COPY --from=builder /workspace/build/libs/mallang-backend-0.0.1-SNAPSHOT.jar /app.jar

# 기본 커맨드: Spring Boot만 실행
ENTRYPOINT ["java", \
           "-Dspring.profiles.active=${PROFILES}", \
           "-Dserver.env=${ENV}", \
           "-jar", "/app.jar"]
