# entrypoint.sh
#!/bin/bash
set -e

# 1) 헤드리스 크롬으로 YouTube 로그인 → 쿠키 생성
python3 /scripts/save_headless_cookies.py

# 2) 생성된 cookies.txt 권한 제한
chmod 600 /tmp/cookies.txt

# 3) Spring Boot 애플리케이션 실행
exec java \
  -Dspring.profiles.active="${PROFILES}" \
  -Dserver.env="${ENV}" \
  -jar /app.jar