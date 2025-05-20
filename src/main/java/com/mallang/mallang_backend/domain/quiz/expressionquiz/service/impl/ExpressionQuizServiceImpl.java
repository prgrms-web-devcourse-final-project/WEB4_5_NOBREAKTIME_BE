package com.mallang.mallang_backend.domain.quiz.expressionquiz.service.impl;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.dto.ExpressionQuizItem;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.dto.ExpressionQuizResponse;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.dto.ExpressionQuizResultSaveRequest;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.entity.ExpressionQuiz;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.repository.ExpressionQuizRepository;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.service.ExpressionQuizService;
import com.mallang.mallang_backend.domain.quiz.expressionquizresult.entity.ExpressionQuizResult;
import com.mallang.mallang_backend.domain.quiz.expressionquizresult.repository.ExpressionQuizResultRepository;
import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItemId;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository.ExpressionBookItemRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.util.japanese.JapaneseSplitter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.mallang.mallang_backend.global.common.Language.ENGLISH;
import static com.mallang.mallang_backend.global.common.Language.JAPANESE;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ExpressionQuizServiceImpl implements ExpressionQuizService {

	private final ExpressionBookRepository expressionBookRepository;
	private final ExpressionBookItemRepository expressionBookItemRepository;
	private final ExpressionRepository expressionRepository;
	private final ExpressionQuizRepository expressionQuizRepository;
	private final ExpressionQuizResultRepository expressionQuizResultRepository;

	@Override
	@Transactional
	public ExpressionQuizResponse generateExpressionBookQuiz(Long expressionBookId, Member member) {
		// 받은 아이디로 문제 만들거 가져오기
		ExpressionBook expressionBook = expressionBookRepository.findByIdAndMember(expressionBookId, member)
			.orElseThrow(() -> new ServiceException(NO_EXPRESSIONBOOK_EXIST_OR_FORBIDDEN));

		List<ExpressionBookItem> items = expressionBookItemRepository.findAllById_ExpressionBookId(expressionBookId);
		if (items.isEmpty()) {
			throw new ServiceException(EXPRESSIONBOOK_IS_EMPTY);
		}

		List<ExpressionQuizItem> quizzes = new ArrayList<>();

		if (expressionBook.getLanguage() == ENGLISH) {
			// 문제 생성
			quizzes = items.stream()
				.map(this::convertToQuizDto)
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(ArrayList::new));
		}

		if (expressionBook.getLanguage() == JAPANESE) {
			// 문제 생성
			quizzes = items.stream()
				.map(this::convertToQuizDtoJapanese)
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(ArrayList::new));
		}


		Collections.shuffle(quizzes);

		// expression quiz 생성
		ExpressionQuiz wordQuiz = ExpressionQuiz.builder()
			.member(member)
			.language(expressionBook.getLanguage())
			.build();

		Long quizId = expressionQuizRepository.save(wordQuiz).getId();
		ExpressionQuizResponse response = new ExpressionQuizResponse();
		response.setQuizId(quizId);
		response.setExpressionBookName(expressionBook.getName());
		response.setQuizItems(quizzes);

		return response;
	}

	/**
	 * 일본어 표현 문장을 단어별 띄어쓰기로 구분
	 * @param item
	 * @return
	 */
	private ExpressionQuizItem convertToQuizDtoJapanese(ExpressionBookItem item) {
		Expression expression = expressionRepository.findById(item.getId().getExpressionId())
			.orElseThrow(() -> new ServiceException(EXPRESSIONBOOK_IS_EMPTY));

		ExpressionBook expressionBook = expressionBookRepository.findById(item.getId().getExpressionBookId())
			.orElseThrow(() -> new ServiceException(EXPRESSION_BOOK_NOT_FOUND));

		String splitSentence = JapaneseSplitter.splitJapanese(expression.getSentence());
		return new ExpressionQuizItem(
			expression.getId(),
			expressionBook.getId(),
			createQuestion(splitSentence),
			splitSentence,
			parseWord(splitSentence),
			expression.getDescription()
		);
	}

	private ExpressionQuizItem convertToQuizDto(ExpressionBookItem item) {
		Expression expression = expressionRepository.findById(item.getId().getExpressionId())
			.orElseThrow(() -> new ServiceException(EXPRESSIONBOOK_IS_EMPTY));

		ExpressionBook expressionBook = expressionBookRepository.findById(item.getId().getExpressionBookId())
			.orElseThrow(() -> new ServiceException(EXPRESSION_BOOK_NOT_FOUND));

		return new ExpressionQuizItem(
			expression.getId(),
			expressionBook.getId(),
			createQuestion(expression.getSentence()),
			expression.getSentence(),
			parseWord(expression.getSentence()),
			expression.getDescription()
		);
	}

	private static String createQuestion(String sentence) {
		// 알파벳, 숫자(\w+)를 {}로 치환, 문장부호는 유지
		return Arrays.stream(sentence.split("\\s+"))
			.map(token -> token.replaceAll("[\\p{L}\\p{N}'’ー々]+", "{}"))
			.collect(Collectors.joining(" "));
	}

	private List<String> parseWord(String sentence) {
		List<String> words = Arrays.stream(sentence.split("\\s+"))
			.map(w -> w.replaceAll("[\\p{Punct}。、「」（）『』【】《》！？]", ""))
			.filter(w -> !w.isBlank())
			.collect(Collectors.toList());
		Collections.shuffle(words);
		return words;
	}

	// 표현 퀴즈 결과 저장
	@Override
	@Transactional
	public void saveExpressionQuizResult(ExpressionQuizResultSaveRequest request, Member member) {
		// 표현함의 표현
		ExpressionBookItemId expressionBookItemId = new ExpressionBookItemId(request.getExpressionBookId(), request.getExpressionId());
		ExpressionBookItem expressionBookItem = expressionBookItemRepository.findById(expressionBookItemId)
			.orElseThrow(() -> new ServiceException(EXPRESSIONBOOK_ITEM_NOT_FOUND));

		// 표현함 퀴즈
		ExpressionQuiz expressionQuiz = expressionQuizRepository.findById(request.getQuizId())
			.orElseThrow(() -> new ServiceException(EXPRESSIONQUIZ_NOT_FOUND));

		// 표현
		Expression expression = expressionRepository.findById(request.getExpressionId())
			.orElseThrow(() -> new ServiceException(EXPRESSION_NOT_FOUND));

		// 표현함
		ExpressionBook expressionBook = expressionBookRepository.findById(request.getExpressionBookId())
			.orElseThrow(() -> new ServiceException(EXPRESSION_BOOK_NOT_FOUND));

		ExpressionQuizResult result = ExpressionQuizResult.builder()
			.expression(expression)
			.expressionBook(expressionBook)
			.expressionQuiz(expressionQuiz)
			.isCorrect(request.isCorrect())
			.build();

		// 학습 표시
		expressionBookItem.updateLearned(true);

		expressionQuizResultRepository.save(result);
	}
}
