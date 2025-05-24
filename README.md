# WEB4_5_NOBREAKTIME_BE
[저희는 쉬는시간 없나요?] 팀의 AI 기반 언어 학습 플랫폼 "말랑(mallang)"

<br/>
<br/>

## 저희는 쉬는시간 없나요? 팀을 소개합니다
|                                           문권이                                           |                                           장무영                                           |                                                        최지선                                                        |                                          엄현수                                           |                                                        서세훈                                                        |                                          신동우                                           |
|:---------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------:|
| <img src="https://avatars.githubusercontent.com/u/102517739?v=4" alt="문권이" width="150"> | <img src="https://avatars.githubusercontent.com/u/136911104?v=4" alt="장무영" width="150"> | <img src="https://avatars.githubusercontent.com/u/192316487?v=4" alt="최지선" width="150"> | <img src="https://avatars.githubusercontent.com/u/55376152?v=4" alt="엄현수" width="150"> | <img src="https://avatars.githubusercontent.com/u/113406474?v=4" alt="서세훈" width="150"> | <img src="https://avatars.githubusercontent.com/u/58596222?v=4" alt="신동우" width="150"> |
|                                          PO                                           |                                         BE 팀장                                          |                                                        TM                                                         |                                           TM                                          |                                                       TM                                                         |                                           TM                                          |
|                          [GitHub](https://github.com/M00NPANG)                          |                          [GitHub](https://github.com/wkdan)                           |                                        [GitHub](https://github.com/wesawth3sun)                                        |                         [GitHub](https://github.com/sameom1048)                          |                                                    [GitHub](https://github.com/sehun-Seo3)                                                     |                         [GitHub](https://github.com/socra167)                          |

<br/>
<br/>

# ☕ Project Overview

## 1. 프로젝트 명
### AI 기반 언어 학습 플랫폼 "말랑(mallang)"

<br/>

## 2. 프로젝트 소개
유튜브 영상을 학습 콘텐츠로 활용하여, 자막 생성, 해석, 표현 분석, 반복 학습 등 실질적인 언어 습득을 지원하는 플랫폼

<br/>

## 3. 기능
### 🔑 핵심 기능 구성

| 기능 영역 | 세부 내용 |
| --- | --- |
| 🧑 **회원 관리** | - 카카오, 구글, 네이버 SSO 로그인<br>- 이메일 로그인 및 회원가입<br>- 이메일 인증 기능 |
| ✨ **영상 탐색** | - 필터를 설정해 영상 검색<br>- 학습 인기 동영상 목록 조회 |
| 🎙️ **자막 생성** | - 유튜브 API로 영상 선택<br>- Clova Speech API로 음성 추출 및 자막 변환 |
| 🌐 **자막 번역** | - 생성된 자막을 Papago API로 번역<br>- 전체 스크립트 제공<br>- 이중 자막 표시 가능 |
| 🧠 **단어/표현 설명 (AI)** | - 단어를 사전 API로 해석 및 설명<br>- 문장별 표현을 LLM 기반으로 의미 및 문맥 설명<br>- 헷갈리는 표현은 대체 표현과 비교 제공 |
| 📝 **개인 학습 도구** | - 단어/표현을 '내 단어장', '내 표현함'에 저장<br>- 저장된 항목 기반 학습 퀴즈 제공 |

### ⏹️ 추가 기능 ⏹️

| 기능 영역 | 세부 내용 |
| --- | --- |
| 📆 **학습 계획** | - 학습 일정 관리<br>- 복습 주기 설정<br>- 오늘의 학습 표현 제공 |
| 📊 **학습 통계** | - 일일 학습량, 학습 현황 요약<br>- 학습 레벨 및 시간 확인<br>- 일정 기간 학습 통계 시각화 |
| 🌟 **추천 콘텐츠** | - 추천 수 높은 영상/강의 메인 노출<br>- 카테고리별 랭킹 제공 |
| 💳 **결제** | - 결제 API 기반 구독 시스템<br>- 베이직, 스탠다드, 프리미엄 플랜 제공 |
| 📚 **추가 단어장/표현함** | - 스탠다드/프리미엄 회원 추가 단어장·표현함 생성 가능 |
| 🌍 **추가 언어 선택** | - 프리미엄 회원 추가 학습 언어 선택 가능 |
| 🧩 **분석** | - 전체 스크립트 요약 제공 |
| 🛠️ **관리자** | - 회원 정보, 게시판, 문의사항 관리 |
| 🕓 **히스토리 저장** | - 최근 학습 영상 히스토리 저장 |



<br/>

## 4. 작업 및 역할 분담

| 이름 | 프로필 | 주요 역할 |
|:----:|:------:|:---------|
| 문권이 | <img src="https://avatars.githubusercontent.com/u/102517739?v=4" alt="문권이" width="100"> | <ul><li>PO (Product Owner)</li><li>프로젝트 일정 관리 및 프론트/백엔드 팀 협업 조율</li><li>프론트엔드 코드 관리<li>기술 문서화</li><li>OpenAI 모듈을 활용한 문제 제작/번역/사전/문법 분석/레벨 분석 기능 개발</li></ul> |
| 장무영 | <img src="https://avatars.githubusercontent.com/u/136911104?v=4" alt="장무영" width="100"> | <ul><li>백엔드 팀장 (BE 리딩 및 기술 문서 작성)</li><li>백엔드 전반 기술 검토 및 품질 관리</li><li>유튜브 Data API를 활용한 영상 조회/선택/히스토리 기능 구현</li><li>영상 내 퀴즈 프로세스 구현</li></ul> |
| 최지선 | <img src="https://avatars.githubusercontent.com/u/192316487?v=4" alt="최지선" width="100"> | <ul><li>AWS 인프라 및 서버 관리</li><li>GitHub Actions 기반 CI/CD 구축 및 운영</li><li>TossPayment API를 활용한 결제 시스템 구현</li><li>구글 / 카카오 / 네이버 소셜 로그인 구현</li><li>TossPayment API를 활용한 결제 시스템 구현</li></ul> |
| 엄현수 | <img src="https://avatars.githubusercontent.com/u/55376152?v=4" alt="엄현수" width="100"> | <ul><li>Clova Speech API를 활용한 음성 추출 및 자막 변환 모듈 개발</li><li>단어장 및 단어, 표현, 대시보드 통합 랜덤 퀴즈 프로세스 구현</li><li>일본어 학습 기능 구현</li></ul> |
| 서세훈 | <img src="https://avatars.githubusercontent.com/u/113406474?v=4" alt="서세훈" width="100"> | <ul><li>유튜브 Data API를 활용한 영상 조회/선택/히스토리 기능 구현</li><li>표현함 기능 구현</li><li>프론트엔드 코드 관리</li></ul> |
| 신동우 | <img src="https://avatars.githubusercontent.com/u/58596222?v=4" alt="신동우" width="100"> | <ul><li>Clova Speech API를 활용한 음성 추출 및 자막 변환 모듈 개발</li><li>단어장 및 단어, 표현, 대시보드 통합 랜덤 퀴즈 프로세스 구현</li><li>일본어 학습 기능 구현</li></ul> |

<br/>

<br/>
<br/>


# 🛠️ Tech
## 기술 스택

### 프론트엔드
<div> 
  <img src="https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white"/>
  <img src="https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=next.js&logoColor=white"/>
</div>

### 백엔드
<div> 
  <img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white"/>
  <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black"/>
</div>

### Database
<div> 
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>
  <img src="https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white"/>
</div>

### IDLE&Tool
<div> 
  <img src="https://img.shields.io/badge/IntelliJ%20IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=white"/>
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white"/>
</div>

### OPEN API
<div> 
    <img src="https://img.shields.io/badge/ChatGPT%20API-412991?style=for-the-badge&logo=openai&logoColor=white"/> 
    <img src="https://img.shields.io/badge/YouTube-FF0000?style=for-the-badge&logo=youtube&logoColor=white"/>
    <img src="https://img.shields.io/badge/Naver-03C75A?style=for-the-badge&logo=naver&logoColor=white"/>
    <img src="https://img.shields.io/badge/Kakao-FFCD00?style=for-the-badge&logo=kakao&logoColor=black"/>
    <img src="https://img.shields.io/badge/Google-4285F4?style=for-the-badge&logo=google&logoColor=white"/>
    <img src="https://img.shields.io/badge/Toss-0064FF?style=for-the-badge&logo=toss&logoColor=white"/>
    <img src="https://img.shields.io/badge/CLOVA-03C75A?style=for-the-badge&logo=naver&logoColor=white"/>
    <img src="https://img.shields.io/badge/yt--dlp-FFEE00?style=for-the-badge&logo=data:image/svg+xml;base64,CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB2aWV3Qm94PSIwIDAgMTAwIDEwMCIgZmlsbD0iI0ZGRUUwMCI+CiAgPGNpcmNsZSBjeD0iNTAiIGN5PSI1MCIgcj0iNTAiIC8+CiAgPHBvbHlnb24gcG9pbnRzPSI0MCwzMCA3MCw1MCA0MCw3MCIgZmlsbD0iIzAwMDAwMCIgLz4KPC9zdmc+Cg=="/>

  
</div>

### Deployment&Infra
<div> 
  <img src="https://img.shields.io/badge/Linux-FCC624?style=for-the-badge&logo=linux&logoColor=black"/>
  <img src="https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonwebservices&logoColor=white"/>
  <img src="https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white"/>
  <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white"/>
  <img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white"/>
</div>

### Monitoring
<div>
  <img src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white"/>
  <img src="https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white"/>
  <img src="https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white"/>
  <img src="https://img.shields.io/badge/Logstash-005571?style=for-the-badge&logo=logstash&logoColor=white"/>
  <img src="https://img.shields.io/badge/Kibana-005571?style=for-the-badge&logo=kibana&logoColor=white"/>
  <img src="https://img.shields.io/badge/Sentry-362D59?style=for-the-badge&logo=sentry&logoColor=white"/>
</div>

### Version management and collaboration tools
<div> 
  <img src="https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white"/>
  <img src="https://img.shields.io/badge/Notion-%23000000.svg?style=for-the-badge&logo=notion&logoColor=white"/>
  <img src="https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)"/>
  <img src="https://img.shields.io/badge/Zep-6001D2?style=for-the-badge&logo=Zep&logoColor=white"/>
  <img src="https://img.shields.io/badge/Google%20Meet-00897B?style=for-the-badge&logo=googlemeet&logoColor=white"/>
  <img src="https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white"/>
</div>


## UML
![image](https://github.com/user-attachments/assets/6cb60bd5-c59f-45b6-9f31-8db06a0f3575)

## ERD
<-- 추가 예정 -->

## System Architecture
<-- 추가 예정 -->

## Flow Chart
<-- 추가 예정 -->
[🗃️ Flow Chart]()

## 🏷️  브랜치 전략

- **`main`**: 실제 운영(배포)되는 코드만 존재
- **`develop`**: 다음 배포를 위한 최신 개발 코드가 모임

### **기능/릴리즈/핫픽스 브랜치**

| 브랜치 유형 | 네이밍 규칙 | 용도/설명 |
| --- | --- | --- |
| **feature** | `feature/기능명` | 새로운 기능 개발 (develop에서 분기) |
| **hotfix** | `hotfix/이슈명` | 운영 중 긴급 버그 수정(main에서 분기) |

> 모든 브랜치는 영어로 작성
> 예시: `feature/oauth-login`, `hotfix/payment-error`

브랜치 요약

- `main`: 운영 배포 코드
- `develop`: 최신 개발 코드
- `feature/*`: 기능 단위 개발
- `hotfix/*`: 운영 긴급 수정

  
## API 명세서
[📝 API 명세서]()
<br/>
<br/>

## 컨벤션
[🎯 Commit Convention](https://github.com/prgrms-web-devcourse-final-project/WEB4_5_NOBREAKTIME_BE/wiki/Git-Flow-%EA%B8%B0%EB%B0%98-%EC%BB%A4%EB%B0%8B-%EC%BB%A8%EB%B2%A4%EC%85%98-&-%EB%B8%8C%EB%9E%9C%EC%B9%98-%EC%A0%84%EB%9E%B5)
