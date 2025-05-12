package com.mallang.mallang_backend.domain.voca.word.service.impl;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;
import static com.mallang.mallang_backend.global.gpt.util.GptScriptProcessor.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mallang.mallang_backend.domain.voca.word.dto.WordSearchResponse;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.word.service.WordService;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import com.mallang.mallang_backend.global.util.redis.RedisDistributedLock;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordServiceImpl implements WordService {

	private final WordRepository wordRepository;
	private final GptService gptService;
	private final RedisDistributedLock redisDistributedLock;
	private final SavedWordResultFetcher savedWordResultFetcher;

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
			String gptResult = gptService.searchWord(word); // DB에 없으면 GPT 호출
			List<Word> generatedWords = parseGptResult(word, gptResult); // GPT 결과 파싱
			wordRepository.saveAll(generatedWords);
			return new WordSearchResponse(convertToResponse(generatedWords)); // 변환 후 반환
		} finally {
			redisDistributedLock.unlock(lockKey, lockValue);
		}
	}

	@Override
	public WordSearchResponse searchWord(String word) {
		List<Word> words = wordRepository.findByWord(word);
		if (words.isEmpty()) {
			throw new ServiceException(ErrorCode.WORD_NOT_FOUND);
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