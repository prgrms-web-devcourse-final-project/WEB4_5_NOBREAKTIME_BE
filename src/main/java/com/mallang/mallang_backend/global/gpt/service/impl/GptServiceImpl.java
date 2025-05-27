package com.mallang.mallang_backend.global.gpt.service.impl;

import static com.mallang.mallang_backend.global.common.Language.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;
import static com.mallang.mallang_backend.global.gpt.util.GptScriptProcessor.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mallang.mallang_backend.domain.dashboard.dto.LevelCheckResponse;
import com.mallang.mallang_backend.domain.stt.converter.TranscriptSegment;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.global.aop.monitor.MonitorExternalApi;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.exception.custom.RetryableException;
import com.mallang.mallang_backend.global.gpt.dto.GptSubtitleResponse;
import com.mallang.mallang_backend.global.gpt.dto.KeywordInfo;
import com.mallang.mallang_backend.global.gpt.dto.Message;
import com.mallang.mallang_backend.global.gpt.dto.OpenAiRequest;
import com.mallang.mallang_backend.global.gpt.dto.OpenAiResponse;
import com.mallang.mallang_backend.global.gpt.service.GptPromptBuilder;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import com.mallang.mallang_backend.global.gpt.util.GptScriptProcessor;

import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GptServiceImpl implements GptService {

	private final WebClient openAiWebClient;
	private final GptPromptBuilder gptPromptBuilder;
	private final MeterRegistry meterRegistry;
	private final ObjectMapper objectMapper;

	private Counter gptCallCounter;

	@PostConstruct
	public void init() {
		this.gptCallCounter = meterRegistry.counter("gpt_api_call_total");
	}

	@Value("${spring.ai.openai.api-key}")
	private String openAiApiKey;

	/**
	 * 단어 검색: 5회 재시도, 1초 간격, 실패 시 fallbackSearchWord 호출
	 */
	@Retry(name = "apiRetry", fallbackMethod = "fallbackSearchWord")
	@Override
	public List<Word> searchWord(String word, Language language) {
		String prompt;
		if (language == ENGLISH) {
			prompt = gptPromptBuilder.buildPromptForSearchWord(word);
			return removeInvalidWord(getGptWordResult(prompt, word));
		}

		if (language == JAPANESE) {
			prompt = gptPromptBuilder.buildPromptForSearchWordJapanese(word);
			return removeInvalidWordJapanese(getGptWordResult(prompt, word));
		}

		throw new ServiceException(LANGUAGE_NOT_CONFIGURED);
	}

	private List<Word> getGptWordResult(String prompt, String word) {
		OpenAiResponse response = callGptApi(prompt);
		validateResponse(response);

		String gptResult = response.getChoices().get(0).getMessage().getContent();
		System.out.println("gptResult = " + gptResult);

		return parseGptResult(word, gptResult);
	}

	/**
	 * 예문이 단어의 형태 그대로 나오는지 검증하고, 형태가 다르면 예외가 발생합니다.
	 * <p>예: 예문이 "He ceases to exist."이고 word가 "cease"인 경우,
	 * "cease"는 정확히 일치하지 않으므로 예외가 발생합니다.</p>
	 *
	 * @param generatedWords 검증할 단어 리스트
	 * @throws ServiceException 예문에 단어가 정확히 포함되지 않은 경우 발생
	 */
	private List<Word> removeInvalidWord(List<Word> generatedWords) {
		List<Word> words = List.copyOf(generatedWords);
		for (Word word : generatedWords) {
			String exampleSentence = word.getExampleSentence();
			String wordText = word.getWord().toLowerCase();
			// 단어가 예문에 정확히 포함되는지 확인
			if (!exampleSentence.toLowerCase().matches(".*\\b" + Pattern.quote(wordText.toLowerCase()) + "\\b.*")) {
				throw new ServiceException(INVALID_WORD);
			}
		}
		return words;
	}

	private List<Word> removeInvalidWordJapanese(List<Word> generatedWords) {
		List<Word> words = List.copyOf(generatedWords);
		for (Word word : generatedWords) {
			String exampleSentence = word.getExampleSentence();
			String wordText = word.getWord();
			// 단어가 예문에 정확히 포함되는지 확인
			if (!exampleSentence.contains(wordText)) {
				throw new ServiceException(INVALID_WORD);
			}
		}
		return words;
	}

	/**
	 * 재시도 소진 후 단어 검색 fallback 처리
	 */
	private List<Word> fallbackSearchWord(String word, Language language, Throwable t) {
		log.warn("[GptService] searchWord fallback 처리, 예외 무시하고 빈 리스트 반환: {}", t.toString());
		return List.of();
	}

	/**
	 * 문장 분석: 5회 재시도, 1초 간격, 실패 시 fallbackAnalyzeSentence 호출
	 */
	@Retry(name = "apiRetry", fallbackMethod = "fallbackAnalyzeSentence")
	@Override
	public String analyzeSentence(String sentence, String translatedSentence, Language language) {
		if (language == ENGLISH) {
			String prompt = gptPromptBuilder.buildPromptForAnalyzeSentence(sentence, translatedSentence);
			return getGptSentenceResult(prompt);
		}
		if (language == JAPANESE) {
			String prompt = gptPromptBuilder.buildPromptForAnalyzeSentenceJapanese(sentence, translatedSentence);
			return getGptSentenceResult(prompt);
		}
		throw new ServiceException(LANGUAGE_NOT_CONFIGURED);
	}

	private String getGptSentenceResult(String prompt) {
		OpenAiResponse response = callGptApi(prompt);
		validateResponse(response);
		return response.getChoices().get(0).getMessage().getContent();
	}


	/**
	 * 재시도 소진 후 문장 분석 fallback 처리
	 */
	private String fallbackAnalyzeSentence(String sentence, String translatedSentence, Language language, Throwable t) {
		log.error("[GptService] analyzeSentence fallback 처리, 예외: {}", t.getMessage());
		throw new ServiceException(API_ERROR);
	}

	/**
	 * 스크립트 분석: 5회 재시도, 1초 간격, 실패 시 fallbackAnalyzeScript 호출
	 */
	@Retry(name = "gptRetry", fallbackMethod = "fallbackAnalyzeScript")
	@Override
	public List<GptSubtitleResponse> analyzeScript(List<TranscriptSegment> segments, Language language) {
		// prompt 생성
		String script = GptScriptProcessor.prepareScriptInputText(segments);
		String prompt;

		if (language == ENGLISH) {
			prompt = gptPromptBuilder.buildPromptForAnalyzeScript(script);
			return removeInvalidKeyword(getGptScriptResult(prompt, segments));
		}
		if (language == JAPANESE) {
			prompt = gptPromptBuilder.buildPromptForAnalyzeScriptJapanese(script);
			return removeInvalidKeywordJapanese(getGptScriptResult(prompt, segments));
		}

		// 회원의 언어가 영상 분석이 불가능한 경우
		throw new ServiceException(LANGUAGE_NOT_CONFIGURED);
	}

	private List<GptSubtitleResponse> getGptScriptResult(String prompt, List<TranscriptSegment> segments) {
		OpenAiResponse response = callGptApi(prompt);
		validateResponse(response);

		// GPT 응답 추출
		String content = response.getChoices().get(0).getMessage().getContent();
		System.out.println("content = " + content);

		// 응답 파싱
		return GptScriptProcessor.parseAnalysisResult(content, segments);
	}

	/**
	 * Original 문장에서 단어 단위로 정확히 일치하지 않는 Keyword 를 제거합니다.
	 *
	 * <p>예: original 이 "He ceases the exist."이고 keyword 가 "cease"인 경우,
	 * "cease"는 정확히 일치하지 않으므로 제거됩니다.</p>
	 *
	 * <p>비교는 다음 조건에 따릅니다:
	 * <ul>
	 *   <li>original 은 공백 기준으로 단어 분리</li>
	 *   <li>단어는 알파벳 외 문자(문장부호 등)를 제거하고 소문자로 변환</li>
	 *   <li>keyword 의 단어도 동일하게 정제 후 비교</li>
	 *   <li>정제된 단어가 정확히 일치하는 경우만 유효</li>
	 * </ul>
	 * </p>
	 *
	 * @param responses OpenAI의 응답 객체 리스트
	 * @return 정확히 일치하는 단어만 Keyword 로 유지된 GptSubtitleResponse 리스트
	 */
	private List<GptSubtitleResponse> removeInvalidKeyword(List<GptSubtitleResponse> responses) {
		return responses.stream()
			.peek(response -> {
				// original 문장을 띄어쓰기로 나누어 Set에 담음
				Set<String> originalWords = Arrays.stream(response.getOriginal().split("\\s+"))
					.map(word -> word.replaceAll("[^a-zA-Z]", "").toLowerCase()) // 문장부호 제거, 소문자화
					.collect(Collectors.toSet());

				List<KeywordInfo> validKeywords = response.getKeywords().stream()
					.filter(keyword -> {
						String word = keyword.getWord().replaceAll("[^a-zA-Z]", "").toLowerCase();
						return originalWords.contains(word);
					})
					.toList();

				response.setKeywords(validKeywords);
			})
			.toList();
	}

	private List<GptSubtitleResponse> removeInvalidKeywordJapanese(List<GptSubtitleResponse> responses) {
		return responses.stream()
			.filter(r -> {
					for (KeywordInfo word : r.getKeywords()) {
						if (!r.getOriginal().contains(word.getWord())) {
							return false;
						}
					}
					return true;
				}
			)
			.toList();
	}

	@Override
	public LevelCheckResponse checkLevel(String wordLevel, String expressionLevel, String wordQuizResultString, String expressionResultString) {
		String prompt = gptPromptBuilder.buildPromptForLevelTestScript(wordLevel, expressionLevel, wordQuizResultString, expressionResultString);

		// GPT 호출
		OpenAiResponse response = callGptApi(prompt);
		validateResponse(response);

		String content = response.getChoices().get(0).getMessage().getContent();

		String newWordLevel = GptScriptProcessor.extractWordLevel(content);
		String newExpressionLevel = GptScriptProcessor.extractExpressionLevel(content);

		if (newWordLevel == null || newExpressionLevel == null) {
			throw new ServiceException(API_ERROR);
		}

		return new LevelCheckResponse(newWordLevel, newExpressionLevel);
	}

	/**
	 * 재시도 소진 후 스크립트(대본) 분석 fallback 처리
	 */
	private List<GptSubtitleResponse> fallbackAnalyzeScript(List<TranscriptSegment> segments, Language language, Throwable t) {
		log.error("[GptService] analyzeScript fallback 처리, 예외: {}", t.getMessage());
		throw new ServiceException(API_ERROR);
	}

	/**
	 * OpenAI API 호출 및 응답 유효성 검증 후 응답 내용 반환
	 *
	 * @param prompt 생성된 프롬프트 문자열
	 * @return GPT 응답의 content 필드 값
	 */
	private String callAndValidate(String prompt) {
		OpenAiResponse response = callGptApi(prompt);
		String content = response.getChoices().get(0).getMessage().getContent();
		log.debug("[GptService] GPT 응답 결과:\n{}", content);
		return content;
	}

	/**
	 * GPT API 호출
	 */
	@MonitorExternalApi(name = "openai")
	@Retry(name = "gptRetry", fallbackMethod = "gptFallback")
	public OpenAiResponse callGptApi(String prompt) {
		try {
			log.debug("[GptService] 요청할 프롬프트:\n{}", prompt);

			OpenAiResponse response = openAiWebClient.post()
				.header("Authorization", "Bearer " + openAiApiKey)
				.bodyValue(buildRequestBody(prompt))
				.retrieve()
				.onStatus(
					status -> status.is4xxClientError() || status.is5xxServerError(),
					clientResponse -> clientResponse.bodyToMono(String.class)
						.map(body -> {
							log.error("[GptService] GPT API 호출 실패. 상태: {}, 응답: {}", clientResponse.statusCode(), body);
							if (clientResponse.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
								return new RetryableException("OpenAI 분당 토큰이 3만 토큰을 초과했습니다.");
							}
							return new ServiceException(GPT_API_CALL_FAILED);
						})
				)
				.bodyToMono(OpenAiResponse.class)
				.block();

			return response;
		} catch (Exception e) {
			if (e instanceof RetryableException) {
				throw e; // 그대로 던지면 Retry가 작동함
			}
			throw new ServiceException(GPT_API_CALL_FAILED, e);
		}
	}

	/**
	 * OpenAI 분당 토큰 초과 실패로 인한 재시도 실패 후 처리
	 */
	public OpenAiResponse gptFallback(Exception ex) {
		// 재시도 실패 후 처리
		log.error("GPT 재시도 실패: {}", ex.getMessage());
		throw new ServiceException(GPT_API_CALL_FAILED);
	}

	/**
	 * GPT 응답이 null이거나 빈 경우를 검증
	 */
	public void validateResponse(OpenAiResponse response) {
		if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
			log.error("[GptService] GPT 응답이 비어있습니다.");
			throw new ServiceException(GPT_RESPONSE_EMPTY);
		}
	}

	/**
	 * GPT API 요청을 위한 OpenAiRequest 객체를 생성.
	 */
	private OpenAiRequest buildRequestBody(String prompt) {
		return new OpenAiRequest(
			"gpt-4o",
			new Message[]{new Message("user", prompt)}
		);
	}
}