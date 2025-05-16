INSERT INTO WORD (WORD, POS, MEANING, DIFFICULTY, EXAMPLE_SENTENCE, TRANSLATED_SENTENCE, CREATED_AT, MODIFIED_AT)
VALUES ('light', '형용사', '가벼운', 'EASY', 'This bag is very light.', '이 가방은 매우 가볍다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('light', '명사', '빛', 'EASY', 'The light was too bright.', '빛이 너무 밝았다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('light', '명사', '전등', 'EASY', 'She turned off the light before leaving.', '그녀는 떠나기 전에 전등을 껐다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('light', '동사', '불을 켜다', 'NORMAL', 'Please light the candles.', '촛불을 켜 주세요.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('light', '동사', '밝게 하다', 'HARD', 'The room is lit by a large window.', '그 방은 큰 창문으로 밝아진다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('light', '형용사', '연한', 'NORMAL', 'She wore a light blue dress.', '그녀는 연한 파란색 드레스를 입었다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- BASIC 플랜 (기간 상관없이 동일한 혜택)
INSERT INTO plan (type, period, amount, description, benefits, created_at, modified_at)
VALUES ('BASIC', 'MONTHLY', 0, '기본 서비스 플랜',
        '{
          "title": "BASIC 플랜",
          "features": [
            "최대 200개의 단어 저장 가능",
            "최대 50개의 표현 저장 및 분석 가능",
            "영상 재생은 월 300시간까지 이용할 수 있어요"
          ],
          "notice": "기본으로 제공되는 무료 기능입니다. 더 많은 기능을 원하시면 스탠다드 또는 프리미엄 플랜을 이용해 보세요."
        }', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

       ('BASIC', 'SIX_MONTHS', 0, '기본 서비스 플랜',
        '{
          "title": "BASIC 플랜",
          "features": [
            "최대 200개의 단어 저장 가능",
            "최대 50개의 표현 저장 및 분석 가능",
            "영상 재생은 월 300시간까지 이용할 수 있어요"
          ],
          "notice": "기본으로 제공되는 무료 기능입니다. 더 많은 기능을 원하시면 스탠다드 또는 프리미엄 플랜을 이용해 보세요."
        }', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

       ('BASIC', 'YEAR', 0, '기본 서비스 플랜',
        '{
          "title": "BASIC 플랜",
          "features": [
            "최대 200개의 단어 저장 가능",
            "최대 50개의 표현 저장 및 분석 가능",
            "영상 재생은 월 300시간까지 이용할 수 있어요"
          ],
          "notice": "기본으로 제공되는 무료 기능입니다. 더 많은 기능을 원하시면 스탠다드 또는 프리미엄 플랜을 이용해 보세요."
        }', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- STANDARD 플랜
       ('STANDARD', 'MONTHLY', 4500, '스탠다드 정기 구독',
        '{
          "title": "스탠다드 정기 구독",
          "features": [
            "무제한 단어장 생성 가능",
            "무제한 단어 저장 가능",
            "무제한 표현 저장 가능",
            "무제한 표현함 생성 가능",
            "무제한 영상 시청 가능"
            "무제한 퀴즈 풀이 가능"
          ],
          "notice": "매달 구독이 자동으로 갱신돼요.",
          "priceInfo": {
            "originalPrice": 4500,
            "discountPrice": 4500,
            "discountRate": 0
          }
        }', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

       ('STANDARD', 'SIX_MONTHS', 24300, '스탠다드 6개월 구독',
        '{
          "title": "스탠다드 6개월 구독",
          "features": [
            "무제한 단어장 생성 가능",
            "무제한 단어 저장 가능",
            "무제한 표현 저장 가능",
            "무제한 표현함 생성 가능",
            "무제한 영상 시청 가능"
            "무제한 퀴즈 풀이 가능"
          ],
          "notice": "6개월을 한 번에 구독하면 10% 저렴하게 이용할 수 있어요.",
          "priceInfo": {
            "originalPrice": 27000,
            "discountPrice": 24300,
            "discountRate": 0.1
          }
        }', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

       ('STANDARD', 'YEAR', 43200, '스탠다드 1년 구독',
        '{
          "title": "스탠다드 1년 구독",
          "features": [
            "무제한 단어장 생성 가능",
            "무제한 단어 저장 가능",
            "무제한 표현 저장 가능",
            "무제한 표현함 생성 가능",
            "무제한 영상 시청 가능"
            "무제한 퀴즈 풀이 가능"
          ],
          "notice": "1년을 한 번에 구독하면 20% 저렴하게 이용할 수 있어요.",
          "priceInfo": {
            "originalPrice": 54000,
            "discountPrice": 43200,
            "discountRate": 0.2
          }
        }', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- PREMIUM 플랜
       ('PREMIUM', 'MONTHLY', 8500, '프리미엄 정기 구독',
        '{
          "title": "프리미엄 정기 구독",
          "features": [
            "모든 언어를 자유롭게 이용",
            "AI 코치와의 실시간 대화 시나리오",
            "인터랙티브 쉐도잉/롤플레잉 챌린지",
            "프리미엄 회원 전용 스터디 그룹",
            "AI 맞춤 커리큘럼/로드맵 제공",
            "학습 데이터 다운로드"
          ],
          "notice": "매달 구독이 자동으로 갱신돼요.",
          "priceInfo": {
            "originalPrice": 8500,
            "discountPrice": 8500,
            "discountRate": 0
          }
        }', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

       ('PREMIUM', 'SIX_MONTHS', 45900, '프리미엄 6개월 구독',
        '{
          "title": "프리미엄 6개월 구독",
          "features": [
            "모든 언어를 자유롭게 이용",
            "AI 코치와의 실시간 대화 시나리오",
            "인터랙티브 쉐도잉/롤플레잉 챌린지",
            "프리미엄 회원 전용 스터디 그룹",
            "AI 맞춤 커리큘럼/로드맵 제공",
            "학습 데이터 다운로드"
          ],
          "notice": "6개월을 한 번에 구독하면 10% 저렴하게 이용할 수 있어요.",
          "priceInfo": {
            "originalPrice": 51000,
            "discountPrice": 45900,
            "discountRate": 0.1
          }
        }', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

       ('PREMIUM', 'YEAR', 81600, '프리미엄 1년 구독',
        '{
          "title": "프리미엄 1년 구독",
          "features": [
            "모든 언어를 자유롭게 이용",
            "AI 코치와의 실시간 대화 시나리오",
            "인터랙티브 쉐도잉/롤플레잉 챌린지",
            "프리미엄 회원 전용 스터디 그룹",
            "AI 맞춤 커리큘럼/로드맵 제공",
            "학습 데이터 다운로드"
          ],
          "notice": "1년을 한 번에 구독하면 20% 저렴하게 이용할 수 있어요.",
          "priceInfo": {
            "originalPrice": 102000,
            "discountPrice": 81600,
            "discountRate": 0.2
          }
        }', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO member (
    email, password, nickname, profile_image_url, login_platform, platform_id,
    language, subscription_type, word_goal, video_goal, word_level, expression_level,
    withdrawal_date, measured_at, created_at, modified_at
) VALUES
-- 1번 회원: 구글 로그인, STANDARD 구독
('user1@gmail.com', 'pw1', '구글유저', 'https://mallang.com/profile1.png', 'GOOGLE', 'google-uid-123',
 'ENGLISH', 'BASIC', 20, 3, 1, 2, NULL, NOW(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 2번 회원: 카카오 로그인, PREMIUM 구독
('user2@kakao.com', 'pw2', '카카오유저', 'https://mallang.com/profile2.png', 'KAKAO', 'kakao-uid-456',
 'ENGLISH', 'PREMIUM', 20, 3, 3, 1, NULL, NOW(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 3번 회원: 네이버 로그인, STANDARD 구독, 탈퇴 처리
('user3@naver.com', 'pw3', '네이버유저', 'https://mallang.com/profile3.png', 'NAVER', 'naver-uid-789',
 'ENGLISH', 'STANDARD', 20, 3, 1, 3, '2024-05-14 12:00:00', NOW(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO payment (member_id,
                     plan_id,
                     order_id,
                     payment_key,
                     total_amount,
                     pay_status,
                     method,
                     approved_at,
                     failure_reason,
                     canceled_reason)
VALUES (1,
        6,
        'test1',
        '5b4f7b3e-7d6a-4c8a-ae12-3f1d8c9a1b2f', -- 랜덤 UUID (실제로는 새로 생성)
        10000,
        'DONE',
        'CARD', -- 결제 방법 (카드)
        NOW(), -- 결제 승인 시간
        NULL, -- 실패 사유 없음
        NULL -- 취소 사유 없음
       );

INSERT INTO payment (
    member_id,
    plan_id,
    order_id,
    payment_key,
    total_amount,
    pay_status,
    method,
    approved_at,
    failure_reason,
    canceled_reason
) VALUES (
             1,
             6,
             'test2',
             'a3e5f7c1-9b2d-4f8a-8c6d-0e1f2a3b4c5d', -- 랜덤 UUID (실제로는 새로 생성)
             5000,
             'ABORTED',
             'PHONE', -- 결제 방법 (휴대폰)
             NOW(),    -- 승인 시간 없음
             'NOT_FOUND_PAYMENT', -- 결제를 찾을 수 없음 (예시)
             NULL     -- 취소 사유 없음
         );

INSERT INTO payment_history (
    payment_id,
    status,
    changed_at,
    reason_detail
) VALUES (
             1,           -- payment_id (위 payment 테이블의 PK)
             'DONE',      -- status (PayStatus의 DONE)
             NOW(),       -- changed_at (현재 시간)
             NULL         -- reason_detail (사유 없음, 필요시 값 입력)
         );