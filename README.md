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
- 고객들이 Coffe Bean package를 온라인 웹 사이트로 주문할 수 있는 서비스입니다.
- Spring을 이용해서 커피 메뉴 데이터를 관리하는 4가지 로직 CRUD를 구현합니다.
- 매일 전날 오후 2시부터 오늘 오후 2시까지의 주문을 모아서 처리합니다.
- 고객들은 주문, 리뷰, 질문, 포인트 정립 등의 기능을 사용할 수 있습니다.
- 관리자는 상품 및 고객 관리 등 쇼핑몰의 전반적인 관리 기능을 수행할 수 있습니다.

<br/>

## 3. 주요 기능
![image](https://github.com/user-attachments/assets/efe5962a-57c2-4059-a27e-54422b831ad6)


<br/>

## 4. 작업 및 역할 분담
   |     |                                                                                         |                                                                                                  |
   |-----|-----------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
   | 문권이 | <img src="https://avatars.githubusercontent.com/u/102517739?v=4" alt="문권이" width="100"> | <ul><li>프로젝트 관리 및 문서화</li><li>팀 리딩 및 커뮤니케이션</li><li>회원가입/로그인</li><li>토큰/세션/시큐리티</li><li>회원 정보 수정</li></ul> |
   | 신동우 | <img src="https://avatars.githubusercontent.com/u/58596222?v=4" alt="신동우" width="100">  | <ul><li>메인 페이지</li><li>장바구니</li><li>주문 관리</li><li>질문 관리</li><li>결제</li></ul>|
   | 최지선 | <img src="https://avatars.githubusercontent.com/u/192316487?v=4" alt="최지선" width="100"> | <ul><li>마이페이지</li><li>주문/배송 관리</li><li>주문/배송 관리</li><li>비회원 주문/배송 관리</li><li>리뷰 및 포인트 관리</li></ul>|
   | 엄현수 | <img src="https://avatars.githubusercontent.com/u/55376152?v=4" alt="엄현수" width="100">  | <ul><li>메인페이지</li><li>장바구니</li><li>주문 관리</li><li>공지사항</li><li>세부 상품 조회</li></ul>|
   | 김경래 | <img src="https://avatars.githubusercontent.com/u/15260002?v=4" alt="김경래" width="100">  | <ul><li>관리자 관리 페이지</li><li>관리자 상품 관리</li><li>관리자 주문 관리</li><li>오후 2시 일괄 배송</li></ul> |

<br/>
<br/>


# 🛠️ Tech
## 기술 스택
### 언어
- JAVA   23
- TypeScript

### 프레임워크 및 라이브러리
- Spring   3.4.2
- Spring  Security
- React   19.0.0
- Next.js   15.1.7
- ShadCN/UI
  
### IED 및 개발 도구
- IntelliJ IDEA
- Visual Studio Code

### 버전 관리 및 협업 도구
- Git
- GitHub
- Slack
- Notion

## UML
![image](https://github.com/user-attachments/assets/621c2429-04f2-4f0c-bf84-0df7d1dde2f7)

## ERD
![Blue White Illustration Group Project Presentation ](https://github.com/user-attachments/assets/a95a0bdf-385c-44d2-8421-2140e5e187fe)

## System Architecture
![image](https://github.com/user-attachments/assets/51b881ad-8acc-47fb-acaf-269dff79be0b)

## Flow Chart
[🗃️ Flow Chart](https://github.com/prgrms-be-devcourse/NBE4-5-1-Team07/wiki/%F0%9F%97%83%EF%B8%8F-Flow-Chart)

## 브랜치 전략
**GitHub Flow** 전략 사용
- **Main Branch**
  - 배포 가능한 상태의 코드를 유지합니다.
  - 모든 배포는 이 브랜치에서 이루어집니다.
- **{name} Branch**
  - 팀원 각자의 개발 브랜치입니다.
  - 모든 기능 개발은 이 브랜치에서 이루어집니다.
- 테스트가 완료되면, Pull Request를 생성하여 Review를 요청합니다. 이 때 타겟은 ```main``` 브랜치입니다.
- Review가 완료되고, 피드백이 모두 반영돠면 해당 ```feature```브랜치를 ```main```브랜치로 **Merge**합니다.
![image](https://github.com/user-attachments/assets/6eb191d6-d686-4e25-a383-6338d02675fc)

## API 명세서
[📝 API 명세서](https://peaceful-acorn-daf.notion.site/API-193d9ae0b864813fa94aea1a6645edbf?pvs=4)
<br/>
<br/>

## 컨벤션
[🎯 Commit Convention](https://github.com/prgrms-be-devcourse/NBE4-5-1-Team07/wiki/%F0%9F%93%8C-Git-Commit-Message-Convention#6-%EC%97%AC%EB%9F%AC%EA%B0%80%EC%A7%80-%ED%95%AD%EB%AA%A9%EC%9D%B4-%EC%9E%88%EB%8B%A4%EB%A9%B4-%EA%B8%80%EB%A8%B8%EB%A6%AC-%EA%B8%B0%ED%98%B8%EB%A5%BC-%ED%86%B5%ED%95%B4-%EA%B0%80%EB%8F%85%EC%84%B1-%EB%86%92%EC%9D%B4%EA%B8%B0)
<br/>
[📌 Code Convention](https://github.com/prgrms-be-devcourse/NBE4-5-1-Team07/wiki/%F0%9F%93%8C-Code-Convention)
