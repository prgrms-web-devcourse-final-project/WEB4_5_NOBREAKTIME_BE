package com.mallang.mallang_backend.test;

import com.mallang.mallang_backend.global.gpt.dto.OpenAiResponse;
import com.mallang.mallang_backend.global.gpt.service.impl.GptServiceImpl;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestService {

    private final GptServiceImpl gptServiceImpl;
    private final MeterRegistry meterRegistry;

    /**
     * GPT 서비스 테스트용 메서드.
     */
    public void testGptService() {
        String testPrompt = "Test prompt for GPT service testing.";

        // 테스트용 프롬프트로 GPT API 호출
        OpenAiResponse response = gptServiceImpl.callGptApi(testPrompt);

        // 타이머 측정을 위한 샘플
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("gpt_test_api_call_seconds")
                .description("TestGptService 호출 시간")
                .tags("status", "success")
                .register(meterRegistry));

        // 응답 유효성 검증
        gptServiceImpl.validateResponse(response);

        log.debug("[GptService] GPT 테스트 응답 결과:\n{}", response.getChoices().get(0).getMessage().getContent());
    }
}
