package com.mallang.mallang_backend.global.gpt.service.impl;

import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.exception.custom.RetryableException;
import com.mallang.mallang_backend.global.gpt.dto.OpenAiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import({ GptServiceImpl.class })
public class GptServiceImplRetryTest {
    @Autowired
    private GptServiceImpl gptService;

    @MockitoBean
    private WebClient openAiWebClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(gptService, "openAiApiKey", "fake-api-key");

        when(openAiWebClient.post()).thenReturn(requestSpec);
        when(requestSpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // WebClient가 항상 429 응답을 반환하게 구성
        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.bodyToMono(OpenAiResponse.class))
                .thenThrow(new RetryableException("Rate limit exceeded"));

    }

    @Test
    @Disabled
    @DisplayName("RetryableException 예외가 발생하면 Retry가 1분동안 10초마다 총 6번 재시도")
    void shouldRetryWhenRetryableExceptionOccurs() {
        // when
        assertThrows(ServiceException.class, () -> gptService.callGptApi("테스트 프롬프트"));

        // then
        // gptRetry 설정: max-attempts = 3 → 총 3번 호출되어야 함
        verify(openAiWebClient, times(6)).post();
    }
}
