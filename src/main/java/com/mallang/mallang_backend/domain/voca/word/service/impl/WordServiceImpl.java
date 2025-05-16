package com.mallang.mallang_backend.domain.voca.word.service.impl;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.voca.word.dto.WordSearchResponse;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.event.NewWordSearchedEvent;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.word.service.WordService;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import com.mallang.mallang_backend.global.util.redis.RedisDistributedLock;
import com.mallang.mallang_backend.global.validation.WordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordServiceImpl implements WordService {

	private final WordRepository wordRepository;
	private final GptService gptService;
	private final RedisDistributedLock redisDistributedLock;
	private final SavedWordResultFetcher savedWordResultFetcher;
	private final MemberRepository memberRepository;
	private final ApplicationEventPublisher publisher;

	@Override
	@Transactional
	public WordSearchResponse savedWord(String word) {
		List<Word> words = wordRepository.findByWord(word); // DB 조회
		if (!words.isEmpty()) {
			return new WordSearchResponse(convertToResponse(words));    // DB에 존재하면 변환하여 반환
		}

		// 락 획득 시도
		String lockKey = "lock:word:saved:" + word;
		String lockValue = UUID.randomUUID().toString();
		long ttlMillis = Duration.ofMinutes(1).toMillis();

		boolean locked = redisDistributedLock.tryLock(lockKey, lockValue, ttlMillis);
		if (!locked) {
			// 락이 사라졌는지 1분간 계속 확인
			boolean lockAvailable = redisDistributedLock.waitForUnlockThenFetch(lockKey, ttlMillis, 1000L);
			// 최대 재시도 시간까지 확인했으나 실패함
			if (!lockAvailable) {
				throw new ServiceException(SAVED_WORD_CONCURRENCY_TIME_OUT);
			}

			// 락이 사라졌으면 다른 작업으로 처리된 결과를 DB에서 찾아서 응답
			words = savedWordResultFetcher.fetchSavedWordResultAfterWait(word);
			if (words.isEmpty()) {
				throw new ServiceException(WORD_SAVE_FAILED);
			}
			return new WordSearchResponse(convertToResponse(words));
		}

		try {
			List<Word> generatedWords = gptService.searchWord(word); // DB에 없으면 GPT 호출
			wordRepository.saveAll(generatedWords);
			return new WordSearchResponse(convertToResponse(generatedWords)); // 변환 후 반환
		} finally {
			redisDistributedLock.unlock(lockKey, lockValue);
		}
	}

	/**
	 * 단어를 검색하여 품사/해석/난이도 목록을 반환합니다.
	 * DB에 없으면 SeviceException을 발생하고 이벤트로 단어를 GPT 검색하여 저장합니다.
	 *
	 * @param word 검색할 단어
	 * @return WordSavedResponse 찾은 단어
	 * @throws ServiceException 단어를 찾을 없을 때 예외 발생
	 */
	@Override
	public WordSearchResponse searchWord(String word, Long memberId) {
		List<Word> words = wordRepository.findByWord(word);
		if (words.isEmpty()) {
			publisher.publishEvent(new NewWordSearchedEvent(word));
			throw new ServiceException(ErrorCode.WORD_NOT_FOUND);
		}

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

		// 단어가 설정한 언어와 맞는지 검증
		if (!WordValidator.isLanguageMatch(word, member.getLanguage())) {
			throw new ServiceException(LANGUAGE_MISMATCH);
		}

		return new WordSearchResponse(convertToResponse(words));
	}

	/**
	 * Word 엔티티 리스트를 WordMeaning 응답 객체 리스트로 변환합니다.
	 *
	 * @param words 변환할 Word 리스트
	 * @return 변환된 WordMeaning 리스트
	 */
	private List<WordSearchResponse.WordMeaning> convertToResponse(List<Word> words) {
		return words.stream()
			.map(WordSearchResponse.WordMeaning::fromEntity)
			.collect(Collectors.toList());
	}
}