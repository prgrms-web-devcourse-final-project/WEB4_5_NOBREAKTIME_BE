INSERT INTO WORD (WORD, POS, MEANING, DIFFICULTY, EXAMPLE_SENTENCE, TRANSLATED_SENTENCE)
VALUES ('light', '형용사', '가벼운', 'EASY', 'This bag is very light.', '이 가방은 매우 가볍다.'),
       ('light', '명사', '빛', 'EASY', 'The light was too bright.', '빛이 너무 밝았다.'),
       ('light', '명사', '전등', 'EASY', 'She turned off the light before leaving.', '그녀는 떠나기 전에 전등을 껐다.'),
       ('light', '동사', '불을 켜다', 'NORMAL', 'Please light the candles.', '촛불을 켜 주세요.'),
       ('light', '동사', '밝게 하다', 'HARD', 'The room is lit by a large window.', '그 방은 큰 창문으로 밝아진다.'),
       ('light', '형용사', '연한', 'NORMAL', 'She wore a light blue dress.', '그녀는 연한 파란색 드레스를 입었다.');


-- BASIC 플랜 (기간 상관없이 동일한 혜택)
INSERT INTO plan (type, period, amount, description, benefits)
VALUES ('BASIC', 'MONTHLY', 0, '기본 서비스 플랜',
        '{
          "title": "BASIC 플랜",
          "features": [
            "최대 200개의 단어 저장 가능",
            "최대 50개의 표현 저장 및 분석 가능",
            "영상 재생은 월 300시간까지 이용할 수 있어요"
          ],
          "notice": "기본으로 제공되는 무료 기능입니다. 더 많은 기능을 원하시면 스탠다드 또는 프리미엄 플랜을 이용해 보세요."
        }'),

       ('BASIC', 'SIX_MONTHS', 0, '기본 서비스 플랜',
        '{
          "title": "BASIC 플랜",
          "features": [
            "최대 200개의 단어 저장 가능",
            "최대 50개의 표현 저장 및 분석 가능",
            "영상 재생은 월 300시간까지 이용할 수 있어요"
          ],
          "notice": "기본으로 제공되는 무료 기능입니다. 더 많은 기능을 원하시면 스탠다드 또는 프리미엄 플랜을 이용해 보세요."
        }'),

       ('BASIC', 'YEAR', 0, '기본 서비스 플랜',
        '{
          "title": "BASIC 플랜",
          "features": [
            "최대 200개의 단어 저장 가능",
            "최대 50개의 표현 저장 및 분석 가능",
            "영상 재생은 월 300시간까지 이용할 수 있어요"
          ],
          "notice": "기본으로 제공되는 무료 기능입니다. 더 많은 기능을 원하시면 스탠다드 또는 프리미엄 플랜을 이용해 보세요."
        }'),

-- STANDARD 플랜
       ('STANDARD', 'MONTHLY', 4500, '스탠다드 정기 구독',
        '{
          "title": "스탠다드 정기 구독",
          "features": [
            "무제한 단어장 생성 가능",
            "무제한 단어 저장 가능",
            "무제한 표현 저장 가능",
            "무제한 표현함 생성 가능",
            "무제한 퀴즈 풀이 가능"
          ],
          "notice": "매달 구독이 자동으로 갱신돼요.",
          "priceInfo": {
            "originalPrice": 4500,
            "discountPrice": 4500,
            "discountRate": 0
          }
        }'),

       ('STANDARD', 'SIX_MONTHS', 24300, '스탠다드 6개월 구독',
        '{
          "title": "스탠다드 6개월 구독",
          "features": [
            "무제한 단어장 생성 가능",
            "무제한 단어 저장 가능",
            "무제한 표현 저장 가능",
            "무제한 표현함 생성 가능",
            "무제한 퀴즈 풀이 가능"
          ],
          "notice": "6개월을 한 번에 구독하면 10% 저렴하게 이용할 수 있어요.",
          "priceInfo": {
            "originalPrice": 27000,
            "discountPrice": 24300,
            "discountRate": 0.1
          }
        }'),

       ('STANDARD', 'YEAR', 43200, '스탠다드 1년 구독',
        '{
          "title": "스탠다드 1년 구독",
          "features": [
            "무제한 단어장 생성 가능",
            "무제한 단어 저장 가능",
            "무제한 표현 저장 가능",
            "무제한 표현함 생성 가능",
            "무제한 퀴즈 풀이 가능"
          ],
          "notice": "1년을 한 번에 구독하면 20% 저렴하게 이용할 수 있어요.",
          "priceInfo": {
            "originalPrice": 54000,
            "discountPrice": 43200,
            "discountRate": 0.2
          }
        }'),

-- PREMIUM 플랜
       ('PREMIUM', 'MONTHLY', 8500, '프리미엄 정기 구독',
        '{
          "title": "프리미엄 정기 구독",
          "features": [
            "모든 언어를 자유롭게 이용할 수 있어요",
            "다양한 언어를 통합적으로 학습할 수 있습니다",
            "언어 제한 없이 모든 기능을 사용할 수 있습니다"
          ],
          "notice": "매달 구독이 자동으로 갱신돼요.",
          "priceInfo": {
            "originalPrice": 8500,
            "discountPrice": 8500,
            "discountRate": 0
          }
        }'),

       ('PREMIUM', 'SIX_MONTHS', 45900, '프리미엄 6개월 구독',
        '{
          "title": "프리미엄 6개월 구독",
          "features": [
            "모든 언어를 자유롭게 이용할 수 있어요",
            "다양한 언어를 통합적으로 학습할 수 있습니다",
            "언어 제한 없이 모든 기능을 사용할 수 있습니다"
          ],
          "notice": "6개월을 한 번에 구독하면 10% 저렴하게 이용할 수 있어요.",
          "priceInfo": {
            "originalPrice": 51000,
            "discountPrice": 45900,
            "discountRate": 0.1
          }
        }'),

       ('PREMIUM', 'YEAR', 81600, '프리미엄 1년 구독',
        '{
          "title": "프리미엄 1년 구독",
          "features": [
            "모든 언어를 자유롭게 이용할 수 있어요",
            "다양한 언어를 통합적으로 학습할 수 있습니다",
            "언어 제한 없이 모든 기능을 사용할 수 있습니다"
          ],
          "notice": "1년을 한 번에 구독하면 20% 저렴하게 이용할 수 있어요.",
          "priceInfo": {
            "originalPrice": 102000,
            "discountPrice": 81600,
            "discountRate": 0.2
          }
        }');

