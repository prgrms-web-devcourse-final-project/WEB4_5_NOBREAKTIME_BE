package com.mallang.mallang_backend.domain.quiz.wordquiz.service.impl;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizResponse;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizResultSaveRequest;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordbookQuizResponse;
import com.mallang.mallang_backend.domain.quiz.wordquiz.entity.WordQuiz;
import com.mallang.mallang_backend.domain.quiz.wordquiz.repository.WordQuizRepository;
import com.mallang.mallang_backend.domain.quiz.wordquizresult.entity.WordQuizResult;
import com.mallang.mallang_backend.domain.quiz.wordquizresult.repository.WordQuizResultRepository;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.video.subtitle.repository.SubtitleRepository;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordStatus;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.mallang.mallang_backend.domain.voca.wordbookitem.repository.WordbookItemRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mallang.mallang_backend.global.constants.AppConstants.DEFAULT_WORDBOOK_NAME;
import static com.mallang.mallang_backend.global.exception.ErrorCode.NO_WORDBOOK_EXIST_OR_FORBIDDEN;
import static com.mallang.mallang_backend.global.exception.ErrorCode.WORDBOOK_IS_EMPTY;
import static com.mallang.mallang_backend.global.util.ReflectionTestUtil.setId;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
public class WordQuizServiceImplTest {

    @InjectMocks
    private WordQuizServiceImpl wordQuizService;

    @Mock
    private WordbookRepository wordbookRepository;

    @Mock
    private WordRepository wordRepository;

    @Mock
    private WordQuizRepository wordQuizRepository;

    @Mock
    private WordbookItemRepository wordbookItemRepository;

    @Mock
    private SubtitleRepository subtitleRepository;

    @Mock
    private WordQuizResultRepository wordQuizResultRepository;

    private Member savedMember;

    private Wordbook savedWordbook;

    @Mock
    private Member member;

    @BeforeEach
    void setUp() {
        // Member
        savedMember = Member.builder()
                .platformId("test_platform_id")
                .language(Language.ENGLISH)
                .build();

        savedMember.updateWordGoal(100);
        setId(savedMember, 1L);

        // Wordbook
        savedWordbook = Wordbook.builder()
                .member(savedMember)
                .name(DEFAULT_WORDBOOK_NAME)
                .language(savedMember.getLanguage())
                .build();
        setId(savedWordbook, 1L);
    }

    @Nested
    @DisplayName("단어장 퀴즈 생성")
    class GenerateWordbookQuizTest {

        @Test
        @DisplayName("성공 - 자막 기반 단어와 커스텀 단어가 혼합된 퀴즈를 생성할 수 있다")
        void generateQuiz_success() {
            Long wordbookId = 1L;

            Wordbook wordbook = Wordbook.builder()
                    .member(savedMember)
                    .language(Language.ENGLISH)
                    .build();
            setId(wordbook, wordbookId);

            WordbookItem customItem = WordbookItem.builder()
                    .wordbook(wordbook)
                    .word("apple")
                    .subtitleId(null)  // 커스텀 단어
                    .build();
            setId(customItem, 101L);

            Word customWord = Word.builder()
                    .word("apple")
                    .exampleSentence("This is an apple.")
                    .translatedSentence("이것은 사과입니다.")
                    .build();

            WordbookItem subtitleItem = WordbookItem.builder()
                    .wordbook(wordbook)
                    .word("banana")
                    .subtitleId(200L)  // 자막 기반 단어
                    .build();
            setId(subtitleItem, 102L);

            Subtitle subtitle = Subtitle.builder()
                    .originalSentence("I like banana.")
                    .translatedSentence("나는 바나나를 좋아해.")
                    .build();
            setId(subtitle, 200L);

            given(wordbookRepository.findByIdAndMember(wordbookId, savedMember)).willReturn(Optional.of(wordbook));
            given(wordbookItemRepository.findAllByWordbook(wordbook)).willReturn(List.of(customItem, subtitleItem));
            given(wordRepository.findByWord("apple")).willReturn(List.of(customWord));
            given(subtitleRepository.findById(200L)).willReturn(Optional.of(subtitle));
            given(wordQuizRepository.save(any())).willAnswer(invocation -> {
                WordQuiz quiz = invocation.getArgument(0);
                setId(quiz, 999L);
                return quiz;
            });

            WordbookQuizResponse response = wordQuizService.generateWordbookQuiz(wordbookId, savedMember);

            assertThat(response.getQuizId()).isEqualTo(999L);
            assertThat(response.getQuizItems()).hasSize(2);
            assertThat(response.getQuizItems()).extracting("word").containsExactlyInAnyOrder("apple", "banana");
            assertThat(response.getQuizItems()).extracting("original").contains("This is an apple.", "I like banana.");
            assertThat(response.getQuizItems()).extracting("meaning").contains("이것은 사과입니다.", "나는 바나나를 좋아해.");
            assertThat(response.getQuizItems()).extracting("question").contains("This is an {}.", "I like {}.");
        }

        @Test
        @DisplayName("실패 - 단어장이 존재하지 않거나 권한이 없을 경우")
        void generateQuiz_wordbookNotFound() {
            Long wordbookId = 1L;
            given(wordbookRepository.findByIdAndMember(wordbookId, savedMember)).willReturn(Optional.empty());

            ServiceException exception = assertThrows(ServiceException.class, () ->
                    wordQuizService.generateWordbookQuiz(wordbookId, savedMember)
            );

            assertThat(exception.getMessageCode()).isEqualTo(NO_WORDBOOK_EXIST_OR_FORBIDDEN.getMessageCode());
        }

        @Test
        @DisplayName("실패 - 단어장이 비어 있는 경우")
        void generateQuiz_emptyWordbook() {
            Long wordbookId = 1L;
            Wordbook wordbook = Wordbook.builder().member(savedMember).build();
            setId(wordbook, wordbookId);

            given(wordbookRepository.findByIdAndMember(wordbookId, savedMember)).willReturn(Optional.of(wordbook));
            given(wordbookItemRepository.findAllByWordbook(wordbook)).willReturn(Collections.emptyList());

            ServiceException exception = assertThrows(ServiceException.class, () ->
                    wordQuizService.generateWordbookQuiz(wordbookId, savedMember)
            );

            assertThat(exception.getMessageCode()).isEqualTo(WORDBOOK_IS_EMPTY.getMessageCode());
        }
    }

    @Test
    @DisplayName("성공 - 퀴즈 결과 저장, 학습 시간 반영 및 단어 학습 표시")
    void saveWordbookQuizResult_success() {
        Long wordbookItemId = 10L;
        Long quizId = 20L;
        Long learningTime = 5000L;

        WordbookItem wordbookItem = WordbookItem.builder()
                .word("test")
                .build();
        setId(wordbookItem, wordbookItemId);

        WordQuiz wordQuiz = WordQuiz.builder()
                .member(savedMember)
                .language(Language.ENGLISH)
                .build();
        setId(wordQuiz, quizId);

        WordQuizResultSaveRequest request = new WordQuizResultSaveRequest();
        request.setQuizId(quizId);
        request.setWordbookItemId(wordbookItemId);
        request.setIsCorrect(true);

        given(wordbookItemRepository.findById(wordbookItemId)).willReturn(Optional.of(wordbookItem));
        given(wordQuizRepository.findById(quizId)).willReturn(Optional.of(wordQuiz));

        wordQuizService.saveWordbookQuizResult(request, savedMember);

        verify(wordQuizResultRepository).save(any(WordQuizResult.class));
        assertThat(wordbookItem.isLearned()).isTrue();
    }

    @Nested
    @DisplayName("통합 퀴즈 생성")
    class WordTotalQuiz {
        @Test
        @DisplayName("실패 - 단어 수 부족 시 예외가 발생해야 한다")
        void testNotEnoughWords_ThrowsException() {
            when(wordbookRepository.findAllByMember(savedMember)).thenReturn(List.of(savedWordbook));
            when(wordbookItemRepository.findAllByWordbookAndWordStatus(savedWordbook, WordStatus.NEW))
                    .thenReturn(createDistinctMockItems(10, WordStatus.NEW));
            when(wordbookItemRepository.findReviewTargetWords(eq(savedMember), any()))
                    .thenReturn(createDistinctMockItems(5, WordStatus.REVIEW_COUNT_1));

            assertThrows(ServiceException.class, () -> {
                wordQuizService.generateWordbookTotalQuiz(savedMember);
            });
        }

        @Test
        @DisplayName("성공 - NEW 50개 + REVIEW 60개 → 정상 생성")
        void testEnoughWords_NormalCase() {
            when(wordbookRepository.findAllByMember(savedMember)).thenReturn(List.of(savedWordbook));
            when(wordbookItemRepository.findAllByWordbookAndWordStatus(savedWordbook, WordStatus.NEW))
                    .thenReturn(createDistinctMockItems(50, WordStatus.NEW));
            when(wordbookItemRepository.findReviewTargetWords(eq(savedMember), any()))
                    .thenReturn(createDistinctMockItems(60, WordStatus.REVIEW_COUNT_1));
            when(wordQuizRepository.save(any())).thenReturn(mock(WordQuiz.class));

            WordQuizResponse response = wordQuizService.generateWordbookTotalQuiz(savedMember);

            assertThat(response).isNotNull();
            assertThat(response.getQuizItems()).hasSize(100);
        }

        @Test
        @DisplayName("성공 - NEW 부족 (20개), REVIEW 충분 (100개) → 정상 생성")
        void testNewTooFew_ReviewEnough() {
            when(wordbookRepository.findAllByMember(savedMember)).thenReturn(List.of(savedWordbook));
            when(wordbookItemRepository.findAllByWordbookAndWordStatus(savedWordbook, WordStatus.NEW))
                    .thenReturn(createDistinctMockItems(20, WordStatus.NEW));
            when(wordbookItemRepository.findReviewTargetWords(eq(savedMember), any()))
                    .thenReturn(createDistinctMockItems(100, WordStatus.REVIEW_COUNT_1));
            when(wordQuizRepository.save(any())).thenReturn(mock(WordQuiz.class));

            WordQuizResponse response = wordQuizService.generateWordbookTotalQuiz(savedMember);

            assertThat(response).isNotNull();
            assertThat(response.getQuizItems()).hasSize(100);
        }

        @Test
        @DisplayName("성공 - REVIEW 부족 (10개), NEW 충분 (100개) → 정상 생성")
        void testReviewTooFew_NewEnough() {
            when(wordbookRepository.findAllByMember(savedMember)).thenReturn(List.of(savedWordbook));
            when(wordbookItemRepository.findAllByWordbookAndWordStatus(savedWordbook, WordStatus.NEW))
                    .thenReturn(createDistinctMockItems(100, WordStatus.NEW));
            when(wordbookItemRepository.findReviewTargetWords(eq(savedMember), any()))
                    .thenReturn(createDistinctMockItems(10, WordStatus.REVIEW_COUNT_1));
            when(wordQuizRepository.save(any())).thenReturn(mock(WordQuiz.class));

            WordQuizResponse response = wordQuizService.generateWordbookTotalQuiz(savedMember);

            assertThat(response).isNotNull();
            assertThat(response.getQuizItems()).hasSize(100);
        }

        @Test
        @DisplayName("성공 - NEW 40개 + REVIEW 60개 → 정상 생성")
        void testExactEnoughWords() {
            when(wordbookRepository.findAllByMember(savedMember)).thenReturn(List.of(savedWordbook));
            when(wordbookItemRepository.findAllByWordbookAndWordStatus(savedWordbook, WordStatus.NEW))
                    .thenReturn(createDistinctMockItems(40, WordStatus.NEW));
            when(wordbookItemRepository.findReviewTargetWords(eq(savedMember), any()))
                    .thenReturn(createDistinctMockItems(60, WordStatus.REVIEW_COUNT_1));
            when(wordQuizRepository.save(any())).thenReturn(mock(WordQuiz.class));

            WordQuizResponse response = wordQuizService.generateWordbookTotalQuiz(savedMember);

            assertThat(response).isNotNull();
            assertThat(response.getQuizItems()).hasSize(100);
        }


        private List<WordbookItem> createDistinctMockItems(int count, WordStatus status) {
            return IntStream.range(0, count)
                    .mapToObj(i -> createMockItem((long) i, status, LocalDateTime.now().minusDays(i)))
                    .collect(Collectors.toList());
        }

        private WordbookItem createMockItem(Long id, WordStatus status, LocalDateTime studiedAt) {
            WordbookItem item = WordbookItem.builder().build();
            ReflectionTestUtils.setField(item, "id", id);
            ReflectionTestUtils.setField(item, "wordStatus", status);
            ReflectionTestUtils.setField(item, "lastStudiedAt", studiedAt);
            return item;
        }

        @Test
        @DisplayName("성공 - 학습 목표 수만큼 NEW + 복습 단어를 비율에 따라 뽑는다")
        void generateWordbookTotalQuiz_success() {
            // given
            savedMember.updateWordGoal(20);

            List<WordbookItem> newWords = IntStream.range(0, 10)
                    .mapToObj(i -> createItem("new" + i, WordStatus.NEW))
                    .collect(Collectors.toCollection(ArrayList::new));

            List<WordbookItem> reviewWords = IntStream.range(0, 20)
                    .mapToObj(i -> {
                        WordStatus status = (i % 2 == 0) ? WordStatus.WRONG : WordStatus.REVIEW_COUNT_1;
                        return createItem("review" + i, status);
                    })
                    .collect(Collectors.toCollection(ArrayList::new));

            when(wordbookRepository.findAllByMember(savedMember)).thenReturn(List.of(savedWordbook));
            when(wordbookItemRepository.findAllByWordbookAndWordStatus(savedWordbook, WordStatus.NEW)).thenReturn(newWords);
            when(wordbookItemRepository.findReviewTargetWords(eq(savedMember), any(LocalDateTime.class))).thenReturn(reviewWords);
            when(wordQuizRepository.save(any(WordQuiz.class))).thenAnswer(invocation -> {
                WordQuiz quiz = invocation.getArgument(0);
                setId(quiz, 999L);
                return quiz;
            });

            // when
            WordQuizResponse response = wordQuizService.generateWordbookTotalQuiz(savedMember);

            // then
            assertThat(response.getQuizId()).isEqualTo(999L);
            assertThat(response.getQuizItems()).hasSize(20);
        }

        private WordbookItem createItem(String word, WordStatus status) {
            WordbookItem item = WordbookItem.builder()
                    .word(word)
                    .build();

            item.updateStatus(status);
            item.updateLastStudiedAt(LocalDateTime.now().minusDays(7));

            setId(item, ThreadLocalRandom.current().nextLong(1, 1000));
            return item;
        }
    }

    @Nested
    @DisplayName("통합 퀴즈 결과")
    class WordbookTotalQuizResult {

        @DisplayName("단어 상태가 NEW일 때 정답이면 CORRECT 상태로 변경된다")
        @Test
        void applyLearningResult_NEW_Correct() {
            // given
            Wordbook wordbook = Wordbook.builder()
                    .name("기본")
                    .language(Language.ENGLISH)
                    .member(mock(Member.class)) // 간단히 목 처리
                    .build();

            WordbookItem item = WordbookItem.builder()
                    .wordbook(wordbook)
                    .word("test")
                    .build();

            // when
            item.applyLearningResult(true);

            // then
            assertThat(item.getWordStatus()).isEqualTo(WordStatus.CORRECT);
            assertThat(item.getLastStudiedAt()).isCloseTo(LocalDateTime.now(), within(1, SECONDS));
        }

        @DisplayName("단어 상태가 NEW일 때 오답이면 WRONG 상태로 변경된다")
        @Test
        void applyLearningResult_NEW_Wrong() {
            // given
            Wordbook wordbook = Wordbook.builder()
                    .name("기본")
                    .language(Language.ENGLISH)
                    .member(mock(Member.class))
                    .build();

            WordbookItem item = WordbookItem.builder()
                    .wordbook(wordbook)
                    .word("test")
                    .build();

            // when
            item.applyLearningResult(false);

            // then
            assertThat(item.getWordStatus()).isEqualTo(WordStatus.WRONG);
            assertThat(item.getLastStudiedAt()).isCloseTo(LocalDateTime.now(), within(1, SECONDS));
        }

        @Test
        @DisplayName("단어 상태가 WRONG일 때 정답을 맞추면 REVIEW_COUNT_1로 변경된다")
        void testApplyLearningResult_WRONG_to_REVIEW_COUNT_1() {
            // given
            Wordbook wordbook = Wordbook.builder()
                    .member(member)
                    .language(Language.ENGLISH)
                    .name("기본")
                    .build();

            WordbookItem item = WordbookItem.builder()
                    .wordbook(wordbook)
                    .word("hello")
                    .build();
            item.updateStatus(WordStatus.WRONG);

            // when
            item.applyLearningResult(true);

            // then
            assertThat(item.getWordStatus()).isEqualTo(WordStatus.REVIEW_COUNT_1);
            assertThat(item.getLastStudiedAt()).isCloseTo(LocalDateTime.now(), within(1, SECONDS));
        }

        @Test
        @DisplayName("단어 상태가 CORRECT일 때 정답을 맞추면 MASTERED로 변경된다")
        void testApplyLearningResult_CORRECT_to_MASTERED() {
            // given
            Wordbook wordbook = Wordbook.builder()
                    .member(member)
                    .language(Language.ENGLISH)
                    .name("기본")
                    .build();

            WordbookItem item = WordbookItem.builder()
                    .wordbook(wordbook)
                    .word("complete")
                    .build();
            item.updateStatus(WordStatus.CORRECT);

            // when
            item.applyLearningResult(true);

            // then
            assertThat(item.getWordStatus()).isEqualTo(WordStatus.MASTERED);
            assertThat(item.getLastStudiedAt()).isCloseTo(LocalDateTime.now(), within(1, SECONDS));
        }

        @Test
        @DisplayName("단어 상태가 CORRECT일 때 오답이면 WRONG으로 변경된다")
        void testApplyLearningResult_CORRECT_to_WRONG() {
            // given
            Wordbook wordbook = Wordbook.builder()
                    .member(member)
                    .language(Language.ENGLISH)
                    .name("기본")
                    .build();

            WordbookItem item = WordbookItem.builder()
                    .wordbook(wordbook)
                    .word("mistake")
                    .build();
            item.updateStatus(WordStatus.CORRECT);

            // when
            item.applyLearningResult(false);

            // then
            assertThat(item.getWordStatus()).isEqualTo(WordStatus.WRONG);
            assertThat(item.getLastStudiedAt()).isCloseTo(LocalDateTime.now(), within(1, SECONDS));
        }
    }
}
