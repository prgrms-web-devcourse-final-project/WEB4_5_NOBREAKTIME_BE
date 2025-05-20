package com.mallang.mallang_backend.global.gpt.service.impl;

import com.mallang.mallang_backend.domain.dashboard.dto.LevelCheckResponse;
import com.mallang.mallang_backend.domain.stt.converter.TranscriptSegment;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.global.aop.monitor.MonitorExternalApi;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.dto.GptSubtitleResponse;
import com.mallang.mallang_backend.global.gpt.dto.Message;
import com.mallang.mallang_backend.global.gpt.dto.OpenAiRequest;
import com.mallang.mallang_backend.global.gpt.dto.OpenAiResponse;
import com.mallang.mallang_backend.global.gpt.service.GptPromptBuilder;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import com.mallang.mallang_backend.global.gpt.util.GptScriptProcessor;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.regex.Pattern;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;
import static com.mallang.mallang_backend.global.gpt.util.GptScriptProcessor.parseGptResult;

/**
 * 부하 테스트 진행을 위해 OpenAI 요청을 Mock Server 연결로 대체한 테스트용 GptService.
 * 외부 테스트 도구를 사용할 때 Mock Server 실행 후 연결
 */
@Slf4j
@RequiredArgsConstructor
public class GptServiceMockImpl implements GptService {

	private final GptPromptBuilder gptPromptBuilder;

	/**
	 * 단어 검색: 5회 재시도, 1초 간격, 실패 시 fallbackSearchWord 호출
	 */
	@Retry(name = "apiRetry", fallbackMethod = "fallbackSearchWord")
	@Override
	public List<Word> searchWord(String word)  {
		String prompt = gptPromptBuilder.buildPromptforSearchWord(word);
		OpenAiResponse response = callGptApiWordAnalyze(prompt);
		validateResponse(response);
		String gptResult = response.getChoices().get(0).getMessage().getContent();
		List<Word> generatedWords = parseGptResult(word, gptResult);
		return removeInvalidWord(generatedWords);
	}

	/**
	 * 예문이 단어의 형태 그대로 나오는지 검증하고, 형태가 다르면 예외가 발생합니다.
	 * <p>예: 예문이 "He ceases to exist."이고 word가 "cease"인 경우,
	 *      "cease"는 정확히 일치하지 않으므로 예외가 발생합니다.</p>
	 * @param generatedWords 검증할 단어 리스트
	 * @throws ServiceException 예문에 단어가 정확히 포함되지 않은 경우 발생
	 */
	private List<Word> removeInvalidWord(List<Word> generatedWords) {
		List<Word> words = List.copyOf(generatedWords);
		for (Word word : generatedWords) {
			String exampleSentence = word.getExampleSentence();
			String wordText = word.getWord().toLowerCase();
			// 단어가 예문에 정확히 포함되는지 확인
			if (!exampleSentence.matches(".*\\b" + Pattern.quote(wordText.toLowerCase()) + "\\b.*")) {
				// 예문에 포함되지 않으면 제거
				throw new ServiceException(INVALID_WORD);
			}
		}
		return words;
	}

	/**
	 * 문장 분석 - 미구현
	 */
	@Retry(name = "apiRetry", fallbackMethod = "fallbackAnalyzeSentence")
	@Override
	public String analyzeSentence(String sentence, String translatedSentence) {
		throw new UnsupportedOperationException();
	}

	/**
	 * OpenAI Mock Server에 스크립트 분석 요청
	 */
	@Retry(name = "apiRetry", fallbackMethod = "fallbackAnalyzeSentence")
	@Override
	public List<GptSubtitleResponse> analyzeScript(List<TranscriptSegment> segments) {
		// prompt 생성
		String script = GptScriptProcessor.prepareScriptInputText(segments);
		String prompt = gptPromptBuilder.buildPromptForAnalyzeScript(script);

		// GPT 호출
		OpenAiResponse response = callGptApiVideoAnalyze(prompt);
		validateResponse(response);

		// GPT 응답 추출
		String content = response.getChoices().get(0).getMessage().getContent();

		// 응답 파싱
		return GptScriptProcessor.parseAnalysisResult(content, segments);
	}

	/**
	 * 레벨 측정 - 동시성 테스트 불필요하므로 미구현
	 */
	@Override
	public LevelCheckResponse checkLevel(String wordLevel, String expressionLevel, String wordQuizResultString, String expressionResultString) {
		throw new UnsupportedOperationException();
	}

	/**
	 * OpenAI 영상 분석 Mock Server 호출
	 */
	@MonitorExternalApi(name = "openai")
	private OpenAiResponse callGptApiVideoAnalyze(String prompt) {
		log.debug("[GptServiceMockImpl] 요청할 프롬프트:\n{}", prompt);

		return WebClient.create("http://localhost:8000") // mock 서버 주소
			.post()
			.uri("/v1/chat/completions") // 두 번째 mock 서버의 엔드포인트
			.bodyValue(buildRequestBody(prompt))
			.retrieve()
			.onStatus(
				status -> status.is4xxClientError() || status.is5xxServerError(),
				clientResponse -> clientResponse.bodyToMono(String.class)
					.flatMap(body -> {
						log.error("[GptServiceMockImpl] GPT API 호출 실패. 상태: {}, 응답: {}", clientResponse.statusCode(), body);
						return Mono.error(new ServiceException(GPT_API_CALL_FAILED));
					})
			)
			.bodyToMono(OpenAiResponse.class)
			.block();
	}

	/**
	 * OpenAI 단어 분석 Mock Server 호출
	 */
	@MonitorExternalApi(name = "openai")
	private OpenAiResponse callGptApiWordAnalyze(String prompt) {
		log.debug("[GptServiceMockImpl] 요청할 프롬프트:\n{}", prompt);

		return WebClient.create("http://localhost:8001") // mock 서버 주소
			.post()
			.uri("/v1/chat/word-analysis") // 두 번째 mock 서버의 엔드포인트
			.bodyValue(buildRequestBody(prompt))
			.retrieve()
			.onStatus(
				status -> status.is4xxClientError() || status.is5xxServerError(),
				clientResponse -> clientResponse.bodyToMono(String.class)
					.flatMap(body -> {
						log.error("[GptServiceMockImpl] GPT API 호출 실패. 상태: {}, 응답: {}", clientResponse.statusCode(), body);
						return Mono.error(new ServiceException(GPT_API_CALL_FAILED));
					})
			)
			.bodyToMono(OpenAiResponse.class)
			.block();
	}

	/**
	 * GPT 응답이 null이거나 빈 경우를 검증
	 */
	private void validateResponse(OpenAiResponse response) {
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
