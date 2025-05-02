package com.mallang.mallang_backend.domain.quiz.expressionquiz.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.controller.ExpressionQuizItem;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.controller.ExpressionQuizResponse;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.entity.ExpressionQuiz;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.repository.ExpressionQuizRepository;
import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItemId;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository.ExpressionBookItemRepository;
import com.mallang.mallang_backend.global.common.Language;

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

	@Test
	@DisplayName("성공 - 표현책 기반 퀴즈 생성")
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
			.sentence("This is apple!")
			.description("이것은 사과입니다.")
			.build();
		setField(expression, "id", expressionId);

		ExpressionBookItem item = ExpressionBookItem.builder()
			.build();
		setField(item, "id", new ExpressionBookItemId(expressionId, expressionBookId)); // 복합키 설정

		// Mock behavior
		when(expressionBookRepository.findByIdAndMember(expressionBookId, member))
			.thenReturn(Optional.of(expressionBook));

		when(expressionBookItemRepository.findAllByExpressionBook(expressionBook))
			.thenReturn(List.of(item));

		when(expressionRepository.findById(expressionId))
			.thenReturn(Optional.of(expression));

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
		assertThat(quizItem.getExpressionQuizItemId()).isEqualTo(100L);
		assertThat(quizItem.getSentence()).isEqualTo("This is apple!");
		assertThat(quizItem.getWords()).containsExactly("This", "is", "apple");
		assertThat(quizItem.getMeaning()).isEqualTo("이것은 사과입니다.");
	}
}
