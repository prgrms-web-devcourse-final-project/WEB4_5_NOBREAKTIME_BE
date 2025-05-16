package com.mallang.mallang_backend.domain.quiz.expressionquiz.service.impl;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.dto.ExpressionQuizItem;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.dto.ExpressionQuizResponse;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.dto.ExpressionQuizResultSaveRequest;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.entity.ExpressionQuiz;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.repository.ExpressionQuizRepository;
import com.mallang.mallang_backend.domain.quiz.expressionquizresult.entity.ExpressionQuizResult;
import com.mallang.mallang_backend.domain.quiz.expressionquizresult.repository.ExpressionQuizResultRepository;
import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItemId;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository.ExpressionBookItemRepository;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.util.ReflectionTestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class ExpressionQuizServiceImplTest {

	@InjectMocks
	private ExpressionQuizServiceImpl expressionQuizService;

	@Mock
	private ExpressionBookRepository expressionBookRepository;

	@Mock
	private ExpressionBookItemRepository expressionBookItemRepository;

	@Mock
	private ExpressionRepository expressionRepository;

	@Mock
	private ExpressionQuizRepository expressionQuizRepository;

	@Mock
	private ExpressionQuizResultRepository expressionQuizResultRepository;

	@Test
	@DisplayName("성공 - 표현함 기반 퀴즈 생성")
	void generateExpressionBookQuiz_success() {
		// given
		Long expressionBookId = 1L;
		Long expressionId = 100L;
		Member member = Member.builder().build();


		ExpressionBook expressionBook = ExpressionBook.builder()
			.member(member)
			.language(Language.ENGLISH)
			.build();
		setField(expressionBook, "id", expressionBookId);

		Expression expression = Expression.builder()
			.sentence("Let's, This is apple!")
			.description("이것은 사과입니다.")
			.build();
		setField(expression, "id", expressionId);

		ExpressionBookItem item = ExpressionBookItem.builder()
			.build();
		setField(item, "id", new ExpressionBookItemId(expressionId, expressionBookId)); // 복합키 설정

		// Mock behavior
		when(expressionBookRepository.findByIdAndMember(expressionBookId, member))
			.thenReturn(Optional.of(expressionBook));

		when(expressionBookItemRepository.findAllById_ExpressionBookId(expressionBookId))
			.thenReturn(List.of(item));

		when(expressionRepository.findById(expressionId))
			.thenReturn(Optional.of(expression));

		when(expressionBookRepository.findById(expressionBookId))
			.thenReturn(Optional.of(expressionBook));

		when(expressionQuizRepository.save(any())).thenAnswer(invocation -> {
			ExpressionQuiz saved = invocation.getArgument(0);
			setField(saved, "id", 999L);
			return saved;
		});

		// when
		ExpressionQuizResponse response = expressionQuizService.generateExpressionBookQuiz(expressionBookId, member);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getQuizId()).isEqualTo(999L);
		assertThat(response.getQuizItems()).hasSize(1);
		ExpressionQuizItem quizItem = response.getQuizItems().get(0);
		assertThat(quizItem.getExpressionId()).isEqualTo(100L);
		assertThat(quizItem.getExpressionBookId()).isEqualTo(1L);
		assertThat(quizItem.getOriginal()).isEqualTo("Let's, This is apple!");
		assertThat(quizItem.getQuestion()).isEqualTo("{}, {} {} {}!");
		assertThat(quizItem.getChoices()).containsExactlyInAnyOrder("This", "is", "apple", "Let's");
		assertThat(quizItem.getMeaning()).isEqualTo("이것은 사과입니다.");
	}

	@Test
	@DisplayName("성공 - 표현 퀴즈 결과 저장 및 학습 표시")
	void saveExpressionQuizResult_success() {
		// given
		Long expressionId = 1L;
		Long expressionBookId = 2L;
		Long quizId = 3L;

		ExpressionBookItemId itemId = new ExpressionBookItemId(expressionId, expressionBookId);
		ExpressionBookItem expressionBookItem = ExpressionBookItem.builder()
			.expressionId(expressionId)
			.expressionBookId(expressionBookId)
			.build();
		ReflectionTestUtils.setField(expressionBookItem, "id", itemId);

		Member member = Member.builder().email("test@example.com").build();
		ReflectionTestUtil.setId(member, 100L);

		Expression expression = Expression.builder()
			.sentence("example")
			.description("desc")
			.sentenceAnalysis("analysis")
			.videos(mock(Videos.class))
			.subtitleAt(LocalTime.of(0, 0, 1))
			.build();
		ReflectionTestUtil.setId(expression, expressionId);

		ExpressionBook expressionBook = ExpressionBook.builder()
			.member(member)
			.name("My Book")
			.language(Language.ENGLISH)
			.build();
		ReflectionTestUtil.setId(expressionBook, expressionBookId);

		ExpressionQuiz expressionQuiz = ExpressionQuiz.builder()
			.member(member)
			.language(Language.ENGLISH)
			.build();
		ReflectionTestUtil.setId(expressionQuiz, quizId);

		ExpressionQuizResultSaveRequest request = new ExpressionQuizResultSaveRequest();
		request.setExpressionId(expressionId);
		request.setExpressionBookId(expressionBookId);
		request.setQuizId(quizId);
		request.setCorrect(true);

		given(expressionBookItemRepository.findById(any(ExpressionBookItemId.class))).willReturn(Optional.of(expressionBookItem));
		given(expressionQuizRepository.findById(quizId)).willReturn(Optional.of(expressionQuiz));
		given(expressionRepository.findById(expressionId)).willReturn(Optional.of(expression));
		given(expressionBookRepository.findById(expressionBookId)).willReturn(Optional.of(expressionBook));

		// when
		expressionQuizService.saveExpressionQuizResult(request, member);

		// then
		verify(expressionQuizResultRepository, times(1)).save(any(ExpressionQuizResult.class));
		assertThat(expressionBookItem.isLearned()).isTrue();
	}
}
