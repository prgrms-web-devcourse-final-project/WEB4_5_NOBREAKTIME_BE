package com.mallang.mallang_backend.domain.quiz.wordquiz.service.impl;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizItem;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizResponse;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizResultSaveRequest;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordbookQuizResponse;
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
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mallang.mallang_backend.domain.quiz.wordquiz.entity.QuizType.INDIVIDUAL;
import static com.mallang.mallang_backend.domain.quiz.wordquiz.entity.QuizType.TOTAL;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

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
	public WordbookQuizResponse generateWordbookQuiz(Long wordbookId, Member member) {
		Wordbook wordbook = wordbookRepository.findByIdAndMember(wordbookId, member)
			.orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

		// 회원이 선택한 단어장이 회원의 현재 설정된 언어와 동일한지 확인
		if (wordbook.getLanguage() != member.getLanguage()) {
			throw new ServiceException(LANGUAGE_MISMATCH);
		}

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
		WordbookQuizResponse response = new WordbookQuizResponse();
		response.setQuizId(quizId);
		response.setWordbookName(wordbook.getName());
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
		dto.setWordbookItemId(id);
		dto.setWord(word);
		dto.setOriginal(original);
		dto.setMeaning(translated);
		dto.setQuestion(createQuestion(word, original));
		return dto;
	}

	private String createQuestion(String word, String original) {
		// 정답 단어를 {}로 대체
		// (?i) 플래그로 대소문자 무시, \b로 단어 경계 매칭
		String regex = "(?i)" + Pattern.quote(word);
		return original.replaceAll(regex, "{}");
	}

	// 단어 결과 저장
	@Transactional
	@Override
	public void saveWordbookQuizResult(WordQuizResultSaveRequest request, Member member) {

		WordbookItem wordbookItem = wordbookItemRepository.findById(request.getWordbookItemId())
			.orElseThrow(() -> new ServiceException(WORDBOOK_ITEM_NOT_FOUND));

		WordQuiz wordQuiz = wordQuizRepository.findById(request.getQuizId())
			.orElseThrow(() -> new ServiceException(WORDQUIZ_NOT_FOUND));

		// 학습 표시
		wordbookItem.updateLearned(true);

		// 기존 결과가 있으면 업데이트, 없으면 새로 저장
		Optional<WordQuizResult> opResult = wordQuizResultRepository.findByWordQuizAndWordbookItem(wordQuiz, wordbookItem);

		if (opResult.isPresent()) {
			WordQuizResult existingResult = opResult.get();
			existingResult.updateIsCorrect(request.isCorrect());
			return;
		}

		WordQuizResult newResult = WordQuizResult.builder()
			.wordQuiz(wordQuiz)
			.wordbookItem(wordbookItem)
			.isCorrect(request.isCorrect())
			.build();

		wordQuizResultRepository.save(newResult);
	}

	// 통합 퀴즈
	@Transactional
	@Override
	public WordQuizResponse generateWordbookTotalQuiz(Member member) {
		int goal = member.getWordGoal();

		// 1. 퀴즈 대상 단어 수집
		List<WordbookItem> newWords = wordbookRepository.findAllByMemberAndLanguage(member, member.getLanguage()).stream()
			.flatMap(wb -> wordbookItemRepository.findAllByWordbookAndWordStatus(wb, WordStatus.NEW).stream())
			.collect(Collectors.toList());
		List<WordbookItem> reviewWords = wordbookItemRepository.findReviewTargetWords(member, LocalDateTime.now());

		// 2. 퀴즈 대상 단어 선정
		List<WordbookItem> selectedNew = new ArrayList<>();
		List<WordbookItem> selectedReview = new ArrayList<>();
		selectWordsForQuiz(goal, newWords, reviewWords, selectedNew, selectedReview);

		// 3. 퀴즈 응답 생성 및 저장
		List<WordQuizItem> quizItems = Stream.concat(selectedNew.stream(), selectedReview.stream())
			.map(this::convertToQuizDto)
			.collect(Collectors.toList());
		Collections.shuffle(quizItems);

		// 4. 통합 퀴즈 생성 및 응답
		WordQuiz wordQuiz = WordQuiz.builder()
			.member(member)
			.quizType(TOTAL)
			.language(member.getLanguage())
			.build();
		Long quizId = wordQuizRepository.save(wordQuiz).getId();

		WordQuizResponse response = new WordQuizResponse();
		response.setQuizId(quizId);
		response.setQuizItems(quizItems);

		return response;
	}

	public void selectWordsForQuiz(
		int goal,
		List<WordbookItem> newWords,
		List<WordbookItem> reviewWords,
		List<WordbookItem> selectedNew,
		List<WordbookItem> selectedReview
	) {

		// 전체 단어 수 부족 시 예외
		if (newWords.size() + reviewWords.size() < goal) {
			throw new ServiceException(ErrorCode.NOT_ENOUGH_WORDS_FOR_QUIZ);
		}

		// 1. 우선 비율 계산 (예: 4:6)
		int newTarget = (int) Math.round(goal * 0.4);
		int reviewTarget = goal - newTarget;

		// 2. 부족한 항목 재조정
		if (newWords.size() < newTarget) {
			reviewTarget += (newTarget - newWords.size());
			newTarget = newWords.size();
		}
		if (reviewWords.size() < reviewTarget) {
			newTarget += (reviewTarget - reviewWords.size());
			reviewTarget = reviewWords.size();
		}

		// 3. 랜덤 추출
		Collections.shuffle(newWords);
		selectedNew.addAll(newWords.subList(0, newTarget));

		// 3-1. 복습 단어 WordStatus로부터 가장 오래된 것부터, 동일하다면 현재로부터 가장 오랜된 것부터
		LocalDateTime now = LocalDateTime.now();
		List<WordbookItem> sortedReviews = reviewWords.stream()
			.sorted(Comparator.comparing((WordbookItem w) -> {
				LocalDateTime due = w.getLastStudiedAt().plus(w.getWordStatus().getReviewIntervalDuration());
				return Duration.between(due, now);
			}).reversed().thenComparing(WordbookItem::getLastStudiedAt))
			.collect(Collectors.toList());

		selectedReview.addAll(sortedReviews.subList(0, reviewTarget));
	}

	// 통합 단어 퀴즈 결과 저장
	@Transactional
	@Override
	public void saveWordbookTotalQuizResult(WordQuizResultSaveRequest request, Member member) {

		WordbookItem wordbookItem = wordbookItemRepository.findById(request.getWordbookItemId())
			.orElseThrow(() -> new ServiceException(WORDBOOK_ITEM_NOT_FOUND));

		// 단어의 현재 상태에 따라 변경
		wordbookItem.applyLearningResult(request.isCorrect());

		WordQuiz wordQuiz = wordQuizRepository.findById(request.getQuizId())
			.orElseThrow(() -> new ServiceException(WORDQUIZ_NOT_FOUND));

		// 기존 결과가 있으면 업데이트, 없으면 새로 저장
		Optional<WordQuizResult> opResult = wordQuizResultRepository.findByWordQuizAndWordbookItem(wordQuiz, wordbookItem);

		if (opResult.isPresent()) {
			WordQuizResult existingResult = opResult.get();
			existingResult.updateIsCorrect(request.isCorrect());
			return;
		}

		WordQuizResult result = WordQuizResult.builder()
			.wordQuiz(wordQuiz)
			.wordbookItem(wordbookItem)
			.isCorrect(request.isCorrect())
			.build();

		wordQuizResultRepository.save(result);
	}
}
