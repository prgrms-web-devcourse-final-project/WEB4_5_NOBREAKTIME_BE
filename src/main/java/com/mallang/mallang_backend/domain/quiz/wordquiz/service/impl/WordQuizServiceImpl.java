package com.mallang.mallang_backend.domain.quiz.wordquiz.service.impl;

import static com.mallang.mallang_backend.domain.quiz.wordquiz.entity.QuizType.INDIVIDUAL;
import static com.mallang.mallang_backend.domain.quiz.wordquiz.entity.QuizType.TOTAL;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizItem;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizResponse;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizResultSaveRequest;
import com.mallang.mallang_backend.domain.quiz.wordquiz.entity.WordQuiz;
import com.mallang.mallang_backend.domain.quiz.wordquiz.repository.WordQuizRepository;
import com.mallang.mallang_backend.domain.quiz.wordquiz.service.WordQuizService;
import com.mallang.mallang_backend.domain.quiz.wordquizresult.entity.WordQuizResult;
import com.mallang.mallang_backend.domain.quiz.wordquizresult.repository.WordQuizResultRepository;
import com.mallang.mallang_backend.domain.video.subtitle.repository.SubtitleRepository;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordStatus;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.mallang.mallang_backend.domain.voca.wordbookitem.repository.WordbookItemRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordQuizServiceImpl implements WordQuizService {

	private final WordQuizResultRepository wordQuizResultRepository;
	private final WordbookRepository wordbookRepository;
	private final WordbookItemRepository wordbookItemRepository;
	private final SubtitleRepository subtitleRepository;
	private final WordRepository wordRepository;
	private final WordQuizRepository wordQuizRepository;

	@Transactional
	@Override
	public WordQuizResponse generateWordbookQuiz(Long wordbookId, Member member) {
		Wordbook wordbook = wordbookRepository.findByIdAndMember(wordbookId, member)
			.orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

		List<WordbookItem> items = wordbookItemRepository.findAllByWordbook(wordbook);
		if (items.isEmpty()) {
			throw new ServiceException(WORDBOOK_IS_EMPTY);
		}

		// 문제 생성
		List<WordQuizItem> quizzes = items.stream()
			.map(this::convertToQuizDto)
			.filter(Objects::nonNull)
			.collect(Collectors.toCollection(ArrayList::new));

		Collections.shuffle(quizzes);

		// word quiz 생성
		WordQuiz wordQuiz = WordQuiz.builder()
			.member(member)
			.quizType(INDIVIDUAL)
			.language(wordbook.getLanguage())
			.build();

		Long quizId = wordQuizRepository.save(wordQuiz).getId();
		WordQuizResponse response = new WordQuizResponse();
		response.setId(quizId);
		response.setQuizItems(quizzes);

		return response;
	}

	private WordQuizItem convertToQuizDto(WordbookItem item) {
		if (item.getSubtitleId() == null) {
			return createQuizFromCustomWord(item);
		} else {
			return createQuizFromSubtitle(item);
		}
	}

	private WordQuizItem createQuizFromCustomWord(WordbookItem item) {
		return wordRepository.findByWord(item.getWord())
			.stream()
			.findAny()
			.map(word -> createDto(item.getId(), item.getWord(), word.getExampleSentence(), word.getTranslatedSentence()))
			.orElse(null);
	}

	private WordQuizItem createQuizFromSubtitle(WordbookItem item) {
		return subtitleRepository.findById(item.getSubtitleId())
			.map(sub -> createDto(item.getId(), item.getWord(), sub.getOriginalSentence(), sub.getTranslatedSentence()))
			.orElse(null);
	}

	private WordQuizItem createDto(Long id, String word, String original, String translated) {
		WordQuizItem dto = new WordQuizItem();
		dto.setId(id);
		dto.setWord(word);
		dto.setOriginal(original);
		dto.setTranslated(translated);
		return dto;
	}

	// 단어 결과 저장
	@Transactional
	@Override
	public void saveWordbookQuizResult(WordQuizResultSaveRequest request, Member member) {

		WordbookItem wordbookItem = wordbookItemRepository.findById(request.getWordbookItemId())
			.orElseThrow(() -> new ServiceException(WORDBOOK_ITEM_NOT_FOUND));

		WordQuiz wordQuiz = wordQuizRepository.findById(request.getQuizId())
			.orElseThrow(() -> new ServiceException(WORDQUIZ_NOT_FOUND));

		WordQuizResult result = WordQuizResult.builder()
			.wordQuiz(wordQuiz)
			.wordbookItem(wordbookItem)
			.isCorrect(request.getIsCorrect())
			.build();

		// 학습 표시
		wordbookItem.updateLearned(true);

		wordQuizResultRepository.save(result);
	}

	// 통합 퀴즈
	@Transactional
	@Override
	public WordQuizResponse generateWordbookTotalQuiz(Member member) {
		int goal = member.getWordGoal();

		List<Wordbook> wordbooks = wordbookRepository.findByMember(member);

		// 1. 조건에 맞는 단어 조회
		List<WordbookItem> newWords = new ArrayList<>();
		for (Wordbook wordbook : wordbooks) {
			newWords.addAll(wordbookItemRepository.findAllByWordbookAndWordStatus(wordbook, WordStatus.NEW));
		}
		List<WordbookItem> reviewWords = wordbookItemRepository.findReviewTargetWords(member, LocalDateTime.now());

		int newCount = newWords.size();
		int reviewCount = reviewWords.size();

		// 2. 전체 단어 수 부족 시 예외
		if (newCount + reviewCount < goal) {
			throw new ServiceException(NOT_ENOUGH_WORDS_FOR_QUIZ);
		}

		// 3. 우선 비율 계산 (예: 4:6)
		int newTarget = (int) Math.round(goal * 0.4); // 이상적인 목표
		int reviewTarget = goal - newTarget; // 이상적인 목표 복습 개수

		// 4. 부족한 항목 재조정
		if (newCount < newTarget) {
			reviewTarget += (newTarget - newCount);
			newTarget = newCount;
		}
		if (reviewCount < reviewTarget) {
			newTarget += (reviewTarget - reviewCount);
			reviewTarget = reviewCount;
		}

		// 5. 랜덤 추출
		Collections.shuffle(newWords);
		Collections.shuffle(reviewWords);

		List<WordbookItem> selectedNew = newWords.subList(0, newTarget);
		List<WordbookItem> selectedReview = reviewWords.subList(0, reviewTarget); // 복습

		// 6. 문제 리스트 구성
		List<WordQuizItem> quizItems = Stream.concat(selectedNew.stream(), selectedReview.stream())
			.map(this::convertToQuizDto)
			.collect(Collectors.toList());

		Collections.shuffle(quizItems); // 문제 순서도 랜덤하게

		// total word quiz 생성
		WordQuiz wordQuiz = WordQuiz.builder()
			.member(member)
			.quizType(TOTAL)
			.language(member.getLanguage())
			.build();

		Long quizId = wordQuizRepository.save(wordQuiz).getId();

		WordQuizResponse response = new WordQuizResponse();
		response.setId(quizId);
		response.setQuizItems(quizItems);

		return response;
	}

	// 통합 단어 퀴즈 결과 저장
	@Transactional
	@Override
	public void saveWordbookTotalQuizResult(WordQuizResultSaveRequest request, Member member) {

		WordbookItem wordbookItem = wordbookItemRepository.findById(request.getWordbookItemId())
			.orElseThrow(() -> new ServiceException(WORDBOOK_ITEM_NOT_FOUND));

		// 단어의 현재 상태에 따라 변경
		wordbookItem.applyLearningResult(request.getIsCorrect());

		WordQuiz wordQuiz = wordQuizRepository.findById(request.getQuizId())
			.orElseThrow(() -> new ServiceException(WORDQUIZ_NOT_FOUND));

		WordQuizResult result = WordQuizResult.builder()
			.wordQuiz(wordQuiz)
			.wordbookItem(wordbookItem)
			.isCorrect(request.getIsCorrect())
			.build();

		wordQuizResultRepository.save(result);
	}
}
