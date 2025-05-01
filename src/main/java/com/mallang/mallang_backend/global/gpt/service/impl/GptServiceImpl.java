package com.mallang.mallang_backend.global.gpt.service.impl;

import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.dto.Message;
import com.mallang.mallang_backend.global.gpt.dto.OpenAiRequest;
import com.mallang.mallang_backend.global.gpt.dto.OpenAiResponse;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import com.mallang.mallang_backend.global.gpt.service.retry.GptRetryableCaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class GptServiceImpl implements GptService {

    private final WebClient openAiWebClient;
    private final GptRetryableCaller gptRetryableCaller;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Override
    public String searchWord(String word) {
        String prompt = buildPromptforSearchWord(word);
        return callAndValidate(prompt);
    }

    @Override
    public String analyzeSentence(String sentence, String translatedSentence) {
        String prompt = buildPromptForAnalyzeSentence(sentence, translatedSentence);
        return callAndValidate(prompt);
    }

    // 결과 반환
    /**
     * OpenAI API 호출 및 응답 유효성 검증 후 응답 내용 반환
     *
     * @param prompt 생성된 프롬프트 문자열
     * @return GPT 응답의 content 필드 값
     */
    private String callAndValidate(String prompt) {
        OpenAiResponse response = gptRetryableCaller.call(openAiWebClient, openAiApiKey, prompt);
        validateResponse(response);
        return response.getChoices().get(0).getMessage().getContent();
    }

    /**
     * GPT 응답이 null이거나 빈 경우를 검증
     */
    private void validateResponse(OpenAiResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            log.error("[GptService] GPT 응답이 비어있습니다.");
            throw new ServiceException(ErrorCode.GPT_RESPONSE_EMPTY);
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

    /**
     * 단어 검색용 프롬프트 생성
     */
    private String buildPromptforSearchWord(String word) {
        return String.format("""
            당신은 영어 단어를 분석하는 도우미입니다.
            사용자가 단어 하나를 입력하면, 그 단어가 가질 수 있는 모든 품사와 해석을 제시하세요.
            
            각 항목은 다음 형식으로 출력하세요:
            {품사} | {해석} | {1~5 숫자}
            
            예시:
            형용사 | 가벼운 | 1 | This bag is very light. | 이 가방은 매우 가볍다.
            명사 | 빛 | 1 | The light was too bright. | 빛이 너무 밝았다.
            
            조건:
            - 난이도는 1, 2, 3, 4, 5 중 하나입니다. (1 = 가장 쉬움, 5 = 가장 어려움)
            - 난이도는 1(가장 쉬움)부터 5(가장 어려움)까지의 숫자로 표시하세요.
            - 품사와 해석은 반드시 한국어로 작성하세요.
            - 예문은 해당 품사로 쓰인 실제 문장을 포함하세요.
            - 예문의 한국어 번역도 반드시 포함하세요.
            - 추가적인 설명 없이 위 형식으로만 출력하세요.
            
            입력된 단어: %s
            """, word);
    }

    /**
     * 문장 분석용 프롬프트 생성
     */
    private String buildPromptForAnalyzeSentence(String sentence, String translatedSentence) {
        return String.format("""
        당신은 영어 문장을 분석해주는 전문 언어 분석 도우미입니다.
        사용자가 원어 문장과 그 번역을 함께 입력하면 다음 정보를 순서대로 분석해 출력하세요.

        - 숙어/표현:핵심 구나 숙어가 있다면 의미와 쓰임을 간단히 설명 (없으면 '없음').
        - 문법 구조:SVO 구조, 시제, 수동태, 조동사 등 주요 문법 요소를 간략히 분석.
        - 화용/의도: 이 문장이 어떤 의도(명령, 요청 등)를 전달하는지, 어떤 상황에서 쓰이는지 분석.

        출력 형식:
        숙어/표현: ...
        문법 구조: ...
        화용/의도: ...

        원문: %s
        번역: %s
        """, sentence, translatedSentence);
    }
}