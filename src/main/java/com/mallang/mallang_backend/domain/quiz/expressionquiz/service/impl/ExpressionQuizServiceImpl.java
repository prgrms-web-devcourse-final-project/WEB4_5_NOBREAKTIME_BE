package com.mallang.mallang_backend.domain.quiz.expressionquiz.service.impl;

import static com.mallang.mallang_backend.global.exception.ErrorCode.EXPRESSIONBOOK_IS_EMPTY;
import static com.mallang.mallang_backend.global.exception.ErrorCode.NO_EXPRESSIONBOOK_EXIST_OR_FORBIDDEN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.controller.ExpressionQuizItem;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.controller.ExpressionQuizResponse;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.entity.ExpressionQuiz;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.repository.ExpressionQuizRepository;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.service.ExpressionQuizService;
import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository.ExpressionBookItemRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpressionQuizServiceImpl implements ExpressionQuizService {

	private final ExpressionBookRepository expressionBookRepository;
	private final ExpressionBookItemRepository expressionBookItemRepository;
	private final ExpressionRepository expressionRepository;
	private final ExpressionQuizRepository expressionQuizRepository;

	public ExpressionQuizResponse generateExpressionBookQuiz(Long expressionBookId, Member member) {
		// 받은 아이디로 문제 만들거 가져오기
		ExpressionBook expressionBook = expressionBookRepository.findByIdAndMember(expressionBookId, member)
			.orElseThrow(() -> new ServiceException(NO_EXPRESSIONBOOK_EXIST_OR_FORBIDDEN));

		List<ExpressionBookItem> items = expressionBookItemRepository.findAllByExpressionBook(expressionBook);
		if (items.isEmpty()) {
			throw new ServiceException(EXPRESSIONBOOK_IS_EMPTY);
		}

		// 문제 생성
		List<ExpressionQuizItem> quizzes = items.stream()
			.map(this::convertToQuizDto)
			.filter(Objects::nonNull)
			.collect(Collectors.toCollection(ArrayList::new));

		Collections.shuffle(quizzes);

		// expression  quiz 생성
		ExpressionQuiz wordQuiz = ExpressionQuiz.builder()
			.member(member)
			.language(expressionBook.getLanguage())
			.build();

		Long quizId = expressionQuizRepository.save(wordQuiz).getId();
		ExpressionQuizResponse response = new ExpressionQuizResponse();
		response.setQuizId(quizId);
		response.setQuizItems(quizzes);

		return response;
	}

	private ExpressionQuizItem convertToQuizDto(ExpressionBookItem item) {
		Expression expression = expressionRepository.findById(item.getId().getExpressionId())
			.orElseThrow(() -> new ServiceException(EXPRESSIONBOOK_IS_EMPTY));

		return new ExpressionQuizItem(
			expression.getId(),
			expression.getSentence(),
			parseWord(expression.getSentence()),
			expression.getDescription()
		);
	}

	private List<String> parseWord(String sentence) {
		// 알파벳만 빼고 제거
		String cleaned = sentence.replaceAll("[^a-zA-Z0-9\\s]", "");

		// 띄어쓰기 단위로 String 리스트화
		return Arrays.asList(cleaned.trim().split("\\s+"));
	}

}
