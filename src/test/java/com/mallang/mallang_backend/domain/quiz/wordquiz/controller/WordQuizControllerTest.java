package com.mallang.mallang_backend.domain.quiz.wordquiz.controller;

import static com.mallang.mallang_backend.global.constants.AppConstants.DEFAULT_WORDBOOK_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizResultSaveRequest;
import com.mallang.mallang_backend.domain.quiz.wordquiz.entity.QuizType;
import com.mallang.mallang_backend.domain.quiz.wordquiz.entity.WordQuiz;
import com.mallang.mallang_backend.domain.quiz.wordquiz.repository.WordQuizRepository;
import com.mallang.mallang_backend.domain.quiz.wordquizresult.repository.WordQuizResultRepository;
import com.mallang.mallang_backend.domain.voca.word.entity.Difficulty;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordStatus;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.mallang.mallang_backend.domain.voca.wordbookitem.repository.WordbookItemRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.util.SecurityTestUtils;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class WordQuizControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WordbookRepository wordbookRepository;

    @Autowired
    private WordbookItemRepository wordbookItemRepository;

    @Autowired
    private WordQuizRepository wordQuizRepository;

    @Autowired
    private WordQuizResultRepository wordQuizResultRepository;

    private Member member;
    private Wordbook wordbook;
    private WordbookItem wordbookItem;
    @Autowired
    private WordRepository wordRepository;

    @Nested
    @DisplayName("퀴즈 테스트")
    class Test1 {

        @BeforeEach
        void setUp() {
            // Member 생성
            member = Member.builder()
                    .platformId("test_123123")
                    .email("test@example.com")
                    .nickname("nickname")
                    .profileImageUrl("profile.jpg")
                    .loginPlatform(LoginPlatform.KAKAO)
                    .language(Language.ENGLISH)
                    .build();
            member.updateWordGoal(100);
            member = memberRepository.save(member);
            SecurityTestUtils.authenticateAs(member);

            // Wordbook 생성
            wordbook = Wordbook.builder()
                    .name(DEFAULT_WORDBOOK_NAME)
                    .language(Language.ENGLISH)
                    .member(member)
                    .build();
            wordbook = wordbookRepository.save(wordbook);

            List<Word> words = List.of(
                    createWord("light", "가벼운", "형용사", "EASY", "This bag is very light.", "이 가방은 매우 가볍다."),
                    createWord("light", "밝은", "형용사", "EASY", "The room is very light in the morning.", "아침에는 방이 매우 밝다."),
                    createWord("light", "빛", "명사", "EASY", "The light from the sun is warm.", "태양에서 나오는 빛은 따뜻하다."),
                    createWord("light", "비추다", "동사", "NORMAL", "She lit the candle to light the room.", "그녀는 방을 비추기 위해 촛불을 켰다."),
                    createWord("light", "전등", "명사", "NORMAL", "Please turn off the light before you go.", "가기 전에 전등을 꺼 주세요."),
                    createWord("tree", "나무", "명사", "EASY", "The tree is full of green leaves.", "그 나무는 푸른 잎으로 가득하다."),
                    createWord("tree", "계통도", "명사", "HARD", "The family tree shows our ancestors.", "가계도는 우리의 조상을 보여준다."),
                    createWord("finish", "끝내다", "동사", "EASY", "I will finish my homework soon.", "나는 곧 숙제를 끝낼 것이다."),
                    createWord("finish", "끝", "명사", "NORMAL", "The finish of the race was exciting.", "경주의 끝은 흥미진진했다."),
                    createWord("run", "달리다", "동사", "EASY", "He can run very fast.", "그는 매우 빨리 달릴 수 있다."),
                    createWord("run", "운영하다", "동사", "NORMAL", "She runs a small bakery.", "그녀는 작은 빵집을 운영한다."),
                    createWord("book", "책", "명사", "EASY", "I read a book every night.", "나는 매일 밤 책을 읽는다."),
                    createWord("book", "예약하다", "동사", "NORMAL", "I booked a table for two.", "나는 두 명 자리를 예약했다."),
                    createWord("watch", "보다", "동사", "EASY", "I watch TV after dinner.", "나는 저녁 먹고 TV를 본다."),
                    createWord("watch", "시계", "명사", "EASY", "This watch is very expensive.", "이 시계는 매우 비싸다."),
                    createWord("change", "변화", "명사", "NORMAL", "There is a big change in the weather.", "날씨에 큰 변화가 있다."),
                    createWord("change", "바꾸다", "동사", "EASY", "I want to change my clothes.", "나는 옷을 갈아입고 싶다."),
                    createWord("play", "놀다", "동사", "EASY", "The children play in the park.", "아이들이 공원에서 논다."),
                    createWord("play", "연극", "명사", "NORMAL", "We watched a famous play last night.", "우리는 어젯밤 유명한 연극을 봤다."),
                    createWord("cold", "추운", "형용사", "EASY", "It is very cold outside.", "밖은 매우 춥다.")
            );

            wordRepository.saveAll(words);

            // NEW 단어 추가
            for (int i = 0; i < 30; i++) {
                wordbookItem = WordbookItem.builder()
                        .wordbook(wordbook)
                        .word("light")
                        .build();
                wordbookItemRepository.save(wordbookItem);
            }

            // REVIEW 단어 추가
            for (int i = 0; i < 70; i++) {
                wordbookItem = WordbookItem.builder()
                        .wordbook(wordbook)
                        .word("tree")
                        .build();
                wordbookItem.updateStatus(WordStatus.WRONG);
                wordbookItem.updateLastStudiedAt(LocalDateTime.now().minusDays(1));
                wordbookItemRepository.save(wordbookItem);
            }

        }

        private Word createWord(String word, String meaning, String pos, String difficulty, String exampleSentence, String translatedSentence) {
            return Word.builder()
                    .word(word)
                    .meaning(meaning)
                    .pos(pos)
                    .difficulty(Difficulty.valueOf(difficulty)) // enum이면 문자열 매핑
                    .exampleSentence(exampleSentence)
                    .translatedSentence(translatedSentence)
                    .build();
        }

        @Test
        @DisplayName("단어장 퀴즈 조회 성공")
        void getWordbookQuiz() throws Exception {

            mockMvc.perform(get("/api/v1/wordbooks/" + wordbook.getId() + "/quiz"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.quizItems").isArray())
                    .andExpect(jsonPath("$.data.quizItems.length()").value(100)); // 100개의 퀴즈 아이템이 반환되어야 합니다.
        }

        @Test
        @DisplayName("통합 퀴즈 조회 성공")
        void getTotalQuiz() throws Exception {
            // NEW 단어를 20개 이상 추가
            for (int i = 0; i < 20; i++) {
                WordbookItem item = WordbookItem.builder()
                        .wordbook(wordbook)
                        .word("word" + i)
                        .build();
                wordbookItemRepository.save(item);

                Word word = Word.builder()
                        .word("word" + i)
                        .meaning("word 단어" + i)
                        .pos("형용사")
                        .difficulty(Difficulty.EASY)
                        .exampleSentence("This is word" + i)
                        .translatedSentence("word 단어" + i + "입니다.")
                        .build();
                wordRepository.save(word);
            }

            mockMvc.perform(get("/api/v1/wordbooks/quiz/total"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.quizItems").isArray())
                    .andExpect(jsonPath("$.data.quizItems.size()").value(100));
        }

        @Test
        @DisplayName("통합 퀴즈 결과 저장 성공")
        void saveQuizResult() throws Exception {
            // Quiz 생성
            Long quizId = wordQuizRepository.save(
                    WordQuiz.builder()
                            .member(member)
                            .language(Language.ENGLISH)
                            .quizType(QuizType.TOTAL)
                            .build()
            ).getId();

            // 퀴즈 결과 저장 요청 객체 생성
            WordQuizResultSaveRequest request = new WordQuizResultSaveRequest();
            request.setQuizId(quizId);
            request.setWordbookItemId(wordbookItem.getId());
            request.setIsCorrect(true);

            // 퀴즈 결과 저장 요청
            mockMvc.perform(post("/api/v1/wordbooks/quiz/total/result")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));

            // 저장된 결과 확인
            var results = wordQuizResultRepository.findAll();
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getIsCorrect()).isTrue();
        }
    }

    @Nested
    @DisplayName("통합 퀴즈 문제 검증")
    class TotalQuizValidation {

        @BeforeEach
        void setUp() {
            // Member 생성
            member = Member.builder()
                    .platformId("test_123123")
                    .email("test@example.com")
                    .nickname("nickname")
                    .profileImageUrl("profile.jpg")
                    .loginPlatform(LoginPlatform.KAKAO)
                    .language(Language.ENGLISH)
                    .build();
            member.updateWordGoal(20);
            member = memberRepository.save(member);
            SecurityTestUtils.authenticateAs(member);

            // Wordbook 생성
            wordbook = Wordbook.builder()
                    .name(DEFAULT_WORDBOOK_NAME)
                    .language(Language.ENGLISH)
                    .member(member)
                    .build();
            wordbook = wordbookRepository.save(wordbook);
        }

        @Test
        @DisplayName("통합 퀴즈 조회 - 목표 단어 20개, 새로운 단어 20개, 복습 단어 20개 -> 복습12 / 새로운단어8개")
        void getTotalQuizContentValidation() throws Exception {
            // 테스트를 위한 추가 단어 삽입 (NEW: 8, REVIEW: 12 이상이 되도록)
            for (int i = 0; i < 20; i++) {
                WordbookItem item = WordbookItem.builder()
                        .wordbook(wordbook)
                        .word("newWord" + i)
                        .build();
                wordbookItemRepository.save(item);

                Word word = Word.builder()
                        .word("newWord" + i)
                        .meaning("새로운 단어" + i)
                        .pos("명사")
                        .difficulty(Difficulty.EASY)
                        .exampleSentence("This is newWord" + i)
                        .translatedSentence("새로운 단어" + i + "입니다.")
                        .build();
                wordRepository.save(word);
            }
            for (int i = 0; i < 20; i++) {
                // 리뷰 대상
                WordbookItem item = WordbookItem.builder()
                        .wordbook(wordbook)
                        .word("reviewWord" + i)
                        .build();
                item.updateStatus(WordStatus.REVIEW_COUNT_1);
                item.updateLastStudiedAt(LocalDateTime.now().minusDays(8));
                wordbookItemRepository.save(item);

                Word word = Word.builder()
                        .word("reviewWord" + i)
                        .meaning("복습 단어" + i)
                        .pos("형용사")
                        .difficulty(Difficulty.EASY)
                        .exampleSentence("This is reviewWord" + i)
                        .translatedSentence("복습 단어" + i + "입니다.")
                        .build();
                wordRepository.save(word);
            }

            // 요청 수행
            MvcResult result = mockMvc.perform(get("/api/v1/wordbooks/quiz/total"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andReturn();

            // 응답 파싱
            String content = result.getResponse().getContentAsString();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(content);
            JsonNode quizItems = root.path("data").path("quizItems");

            // 검증 로직
            int total = quizItems.size();
            int newCount = 0;
            int reviewCount = 0;

            for (JsonNode item : quizItems) {
                String word = item.get("word").asText();
                if (word.startsWith("newWord")) {
                    newCount++;
                }
                if (word.startsWith("reviewWord")) {
                    reviewCount++;
                }
            }

            // NEW + REVIEW 합이 목표 수치와 일치하는지
            assertThat(total).isEqualTo(newCount + reviewCount);
            assertEquals(8, newCount, "NEW 단어 수가 8이 아님");
            assertEquals(12, reviewCount, "REVIEW 단어 수가 12이 아님");
        }

        @Test
        @DisplayName("통합 퀴즈 조회 - 목표 단어 20개, 새로운 단어 8개, 복습 단어 8개, 일반 단어(복습 대상 아님) 10개 -> 퀴즈 조회 실패")
        void getTotalQuizContentValidation2() throws Exception {
            // 테스트를 위한 추가 단어 삽입 (NEW: 8, REVIEW: 8 이상이 되도록)
            // 새로운 단어
            for (int i = 0; i < 8; i++) {
                WordbookItem item = WordbookItem.builder()
                        .wordbook(wordbook)
                        .word("newWord" + i)
                        .build();
                wordbookItemRepository.save(item);

                Word word = Word.builder()
                        .word("newWord" + i)
                        .meaning("새로운 단어" + i)
                        .pos("명사")
                        .difficulty(Difficulty.EASY)
                        .exampleSentence("This is newWord" + i)
                        .translatedSentence("새로운 단어" + i + "입니다.")
                        .build();
                wordRepository.save(word);
            }
            // 리뷰 대상
            for (int i = 0; i < 8; i++) {
                WordbookItem item = WordbookItem.builder()
                        .wordbook(wordbook)
                        .word("reviewWord" + i)
                        .build();
                item.updateStatus(WordStatus.REVIEW_COUNT_1);
                item.updateLastStudiedAt(LocalDateTime.now().minusDays(8));
                wordbookItemRepository.save(item);

                Word word = Word.builder()
                        .word("reviewWord" + i)
                        .meaning("복습 단어" + i)
                        .pos("형용사")
                        .difficulty(Difficulty.EASY)
                        .exampleSentence("This is reviewWord" + i)
                        .translatedSentence("복습 단어" + i + "입니다.")
                        .build();
                wordRepository.save(word);
            }
            // 그냥 단어 (대상아님)
            for (int i = 0; i < 10; i++) {
                WordbookItem item = WordbookItem.builder()
                        .wordbook(wordbook)
                        .word("normalWord" + i)
                        .build();
                item.updateStatus(WordStatus.REVIEW_COUNT_1);
                item.updateLastStudiedAt(LocalDateTime.now().minusDays(2));
                wordbookItemRepository.save(item);

                Word word = Word.builder()
                        .word("normalWord" + i)
                        .meaning("대상이 아닌 단어" + i)
                        .pos("형용사")
                        .difficulty(Difficulty.EASY)
                        .exampleSentence("This is normalWord" + i)
                        .translatedSentence("대상이 아닌 단어" + i + "입니다.")
                        .build();
                wordRepository.save(word);
            }

            // 요청 수행
            mockMvc.perform(get("/api/v1/wordbooks/quiz/total"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("400-1"))
                    .andExpect(jsonPath("$.message").value("퀴즈 생성에 가능한 단어가 부족합니다."))
                    .andReturn();
        }

        @Test
        @DisplayName("통합 퀴즈 조회 - 목표 단어 20개, 새로운 단어 16개, 복습 단어 6개 -> 복습6 / 새로운단어14")
        void getTotalQuizContentValidation3() throws Exception {
            // 테스트를 위한 추가 단어 삽입 (NEW: 16, REVIEW: 10 이상이 되도록)
            for (int i = 0; i < 16; i++) {
                WordbookItem item = WordbookItem.builder()
                        .wordbook(wordbook)
                        .word("newWord" + i)
                        .build();
                wordbookItemRepository.save(item);

                Word word = Word.builder()
                        .word("newWord" + i)
                        .meaning("새로운 단어" + i)
                        .pos("명사")
                        .difficulty(Difficulty.EASY)
                        .exampleSentence("This is newWord" + i)
                        .translatedSentence("새로운 단어" + i + "입니다.")
                        .build();
                wordRepository.save(word);
            }
            for (int i = 0; i < 6; i++) {
                // 리뷰 대상
                WordbookItem item = WordbookItem.builder()
                        .wordbook(wordbook)
                        .word("reviewWord" + i)
                        .build();
                item.updateStatus(WordStatus.REVIEW_COUNT_1);
                item.updateLastStudiedAt(LocalDateTime.now().minusDays(8));
                wordbookItemRepository.save(item);

                Word word = Word.builder()
                        .word("reviewWord" + i)
                        .meaning("복습 단어" + i)
                        .pos("형용사")
                        .difficulty(Difficulty.EASY)
                        .exampleSentence("This is reviewWord" + i)
                        .translatedSentence("복습 단어" + i + "입니다.")
                        .build();
                wordRepository.save(word);
            }

            // 요청 수행
            MvcResult result = mockMvc.perform(get("/api/v1/wordbooks/quiz/total"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andReturn();

            // 응답 파싱
            String content = result.getResponse().getContentAsString();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(content);
            JsonNode quizItems = root.path("data").path("quizItems");

            // 검증 로직
            int total = quizItems.size();
            int newCount = 0;
            int reviewCount = 0;

            for (JsonNode item : quizItems) {
                String word = item.get("word").asText();
                if (word.startsWith("newWord")) {
                    newCount++;
                }
                if (word.startsWith("reviewWord")) {
                    reviewCount++;
                }
            }

            // NEW + REVIEW 합이 목표 수치와 일치하는지
            assertThat(total).isEqualTo(newCount + reviewCount);
            assertEquals(14, newCount, "NEW 단어 수가 14이 아님");
            assertEquals(6, reviewCount, "REVIEW 단어 수가 16이 아님");
        }

        @Test
        @DisplayName("통합 퀴즈 조회 - 복습 단어는 복습 시점(복습 대상이 된 시점) 기준으로 오래된 순서대로 포함된다")
        void getTotalQuizContent_ReviewWordsSortedByReviewDueDate() throws Exception {
            // given: 복습 단어 3개 삽입, 각각 다른 lastStudiedAt 설정
            LocalDateTime now = LocalDateTime.now();

            // REVIEW_COUNT_1, 복습 대상 날 부터 11일, 12일, 13일, ... 20일 뒤까지
            for (int i = 0; i < 10; i++) {
                WordbookItem item = WordbookItem.builder()
                        .wordbook(wordbook)
                        .word("reviewWord-R1-" + i)
                        .build();

                item.updateStatus(WordStatus.REVIEW_COUNT_1); // +7일 후 복습 대상
                item.updateLastStudiedAt(now.minusDays(7 + i)); // 복습 대상 날 부터 11일, 12일, 13일, ... 20일 뒤

                wordbookItemRepository.save(item);

                Word word = Word.builder()
                        .word("reviewWord-R1-" + i)
                        .meaning("복습 단어" + i)
                        .pos("형용사")
                        .difficulty(Difficulty.EASY)
                        .exampleSentence("This is reviewWord" + i)
                        .translatedSentence("복습 단어" + i + "입니다.")
                        .build();

                wordRepository.save(word);
            }

            // REVIEW_COUNT_2, 복습 대상 날 부터 1일, 2일, 3일, ... 10일 뒤까지
            for (int i = 0; i < 10; i++) {
                WordbookItem item = WordbookItem.builder()
                        .wordbook(wordbook)
                        .word("reviewWord-R2-" + i)
                        .build();

                item.updateStatus(WordStatus.REVIEW_COUNT_2); // +30일 후 복습 대상
                item.updateLastStudiedAt(now.minusDays(30 + i)); // 복습 대상 날 부터 1일, 2일, 3일, ... 10일 뒤

                wordbookItemRepository.save(item);

                Word word = Word.builder()
                        .word("reviewWord-R2-" + i)
                        .meaning("복습 단어" + i)
                        .pos("형용사")
                        .difficulty(Difficulty.EASY)
                        .exampleSentence("This is reviewWord" + i)
                        .translatedSentence("복습 단어" + i + "입니다.")
                        .build();

                wordRepository.save(word);
            }

            // 목표 수를 맞추기 위해 새로운 단어도 추가
            for (int i = 0; i < 10; i++) {
                WordbookItem item = WordbookItem.builder()
                        .wordbook(wordbook)
                        .word("newWord" + i)
                        .build();

                wordbookItemRepository.save(item);

                Word word = Word.builder()
                        .word("newWord" + i)
                        .meaning("새로운 단어" + i)
                        .pos("명사")
                        .difficulty(Difficulty.EASY)
                        .exampleSentence("This is newWord" + i)
                        .translatedSentence("새로운 단어" + i + "입니다.")
                        .build();

                wordRepository.save(word);
            }

            // when
            MvcResult result = mockMvc.perform(get("/api/v1/wordbooks/quiz/total"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andReturn();

            // then
            String content = result.getResponse().getContentAsString();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(content);
            JsonNode quizItems = root.path("data").path("quizItems");

            // 복습 단어만 필터링하고 순서 검증
            List<String> reviewWords = new ArrayList<>();
            for (JsonNode item : quizItems) {
                String word = item.get("word").asText();
                if (word.startsWith("reviewWord")) {
                    reviewWords.add(word);
                }
            }

            System.out.println(result.getResponse().getContentAsString());

            // 정렬 순서 검증: reviewWord2 (10일전) → reviewWord1 (9일전) → reviewWord0 (8일전)
            assertThat(reviewWords)
                    .contains("reviewWord-R2-9", "reviewWord-R1-9", "reviewWord-R2-8", "reviewWord-R1-8",
                            "reviewWord-R2-7", "reviewWord-R1-7", "reviewWord-R2-6", "reviewWord-R1-6",
                            "reviewWord-R2-5", "reviewWord-R1-5", "reviewWord-R2-4", "reviewWord-R1-4");
        }
    }
}