package com.mallang.mallang_backend.global.gpt.service.impl;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.mallang.mallang_backend.domain.dashboard.dto.LevelCheckResponse;
import com.mallang.mallang_backend.domain.stt.converter.TranscriptSegment;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class GptServiceImpl implements GptService {

    private final WebClient openAiWebClient;
    private final GptPromptBuilder gptPromptBuilder;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    /**
     * 단어 검색: 5회 재시도, 1초 간격, 실패 시 fallbackSearchWord 호출
     */
    @Retry(name = "apiRetry", fallbackMethod = "fallbackSearchWord")
    @Override
    public String searchWord(String word)  {
        String prompt = gptPromptBuilder.buildPromptforSearchWord(word);
        OpenAiResponse response = callGptApi(prompt);
        validateResponse(response);
        return response.getChoices().get(0).getMessage().getContent();
    }

    /**
     * 재시도 소진 후 단어 검색 fallback 처리
     */
    private String fallbackSearchWord(String word, Throwable t) {
        log.error("[GptService] searchWord fallback 처리, 예외: {}", t.getMessage());
        throw new ServiceException(API_ERROR);
    }

    /**
     * 문장 분석: 5회 재시도, 1초 간격, 실패 시 fallbackAnalyzeSentence 호출
     */
    @Retry(name = "apiRetry", fallbackMethod = "fallbackAnalyzeSentence")
    @Override
    public String analyzeSentence(String sentence, String translatedSentence) {
        String prompt = gptPromptBuilder.buildPromptForAnalyzeSentence(sentence, translatedSentence);
        OpenAiResponse response = callGptApi(prompt);
        validateResponse(response);
        return response.getChoices().get(0).getMessage().getContent();
    }

    /**
     * 재시도 소진 후 문장 분석 fallback 처리
     */
    private String fallbackAnalyzeSentence(String sentence, String translatedSentence, Throwable t) {
        log.error("[GptService] analyzeSentence fallback 처리, 예외: {}", t.getMessage());
        throw new ServiceException(API_ERROR);
    }

    /**
     * 스크립트 분석: 5회 재시도, 1초 간격, 실패 시 fallbackAnalyzeSentence 호출
     */
    @Retry(name = "apiRetry", fallbackMethod = "fallbackAnalyzeSentence")
    @Override
    public List<GptSubtitleResponse> analyzeScript(List<TranscriptSegment> segments) {
        // prompt 생성
        String script = GptScriptProcessor.prepareScriptInputText(segments);
        String prompt = gptPromptBuilder.buildPromptForAnalyzeScript(script);

        // GPT 호출
        OpenAiResponse response = callGptApi(prompt);
        validateResponse(response);

        // GPT 응답 추출
        String content = response.getChoices().get(0).getMessage().getContent();

        // 응답 파싱
        return GptScriptProcessor.parseAnalysisResult(content, segments);
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
    private String fallbackAnalyzeScript(List<TranscriptSegment> segments, Throwable t) {
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
    private OpenAiResponse callGptApi(String prompt) {
        log.debug("[GptService] 요청할 프롬프트:\n{}", prompt);

        return openAiWebClient.post()
            .header("Authorization", "Bearer " + openAiApiKey)
            .bodyValue(buildRequestBody(prompt))
            .retrieve()
            .onStatus(
                status -> status.is4xxClientError() || status.is5xxServerError(),
                clientResponse -> clientResponse.bodyToMono(String.class)
                    .map(body -> {
                        log.error("[GptService] GPT API 호출 실패. 상태: {}, 응답: {}", clientResponse.statusCode(), body);
                        return new ServiceException(GPT_API_CALL_FAILED);
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