package com.mallang.mallang_backend.domain.quiz.wordquiz.service.impl;

import static com.mallang.mallang_backend.global.exception.ErrorCode.NO_WORDBOOK_EXIST_OR_FORBIDDEN;
import static com.mallang.mallang_backend.global.exception.ErrorCode.WORDBOOK_IS_EMPTY;
import static com.mallang.mallang_backend.global.util.ReflectionTestUtil.setId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizItemDto;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.video.subtitle.repository.SubtitleRepository;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.mallang.mallang_backend.domain.voca.wordbookitem.repository.WordbookItemRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
public class WordQuizServiceImplTest {

	@InjectMocks
	private WordQuizServiceImpl wordQuizService;

	@Mock
	private WordbookRepository wordbookRepository;

	@Mock
	private WordRepository wordRepository;

	@Mock
	private WordbookItemRepository wordbookItemRepository;

	@Mock
	private SubtitleRepository subtitleRepository;

	@Nested
	@DisplayName("단어장 퀴즈 생성")
	class GenerateWordbookQuizTest {

		private Member savedMember;

		@BeforeEach
		void setUp() {
			// Member
			savedMember = Member.builder()
				.language(Language.ENGLISH)
				.build();
			setId(savedMember, 1L);
		}

		@Test
		@DisplayName("성공 - 자막 기반 단어와 커스텀 단어가 혼합된 퀴즈를 생성할 수 있다")
		void generateQuiz_success() {
			Long wordbookId = 1L;

			Wordbook wordbook = Wordbook.builder()
				.member(savedMember)
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
				.originalSentence("I like bananas.")
				.translatedSentence("나는 바나나를 좋아해.")
				.build();
			setId(subtitle, 200L );

			given(wordbookRepository.findByIdAndMember(wordbookId, savedMember)).willReturn(Optional.of(wordbook));
			given(wordbookItemRepository.findAllByWordbook(wordbook))
				.willReturn(new ArrayList<>(List.of(customItem, subtitleItem)));

			given(wordRepository.findByWord("apple")).willReturn(List.of(customWord));
			given(subtitleRepository.findById(200L)).willReturn(Optional.of(subtitle));

			List<WordQuizItemDto> quizList = wordQuizService.generateWordbookQuiz(wordbookId, savedMember);

			assertThat(quizList).hasSize(2);
			assertThat(quizList).extracting("word").containsExactlyInAnyOrder("apple", "banana");
			assertThat(quizList).extracting("original").contains("This is an apple.", "I like bananas.");
			assertThat(quizList).extracting("translated").contains("이것은 사과입니다.", "나는 바나나를 좋아해.");
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

}
