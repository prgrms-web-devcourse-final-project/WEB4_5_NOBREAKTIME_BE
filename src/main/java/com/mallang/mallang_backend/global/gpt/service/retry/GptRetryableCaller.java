package com.mallang.mallang_backend.global.gpt.service.retry;

import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.dto.Message;
import com.mallang.mallang_backend.global.gpt.dto.OpenAiRequest;
import com.mallang.mallang_backend.global.gpt.dto.OpenAiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Component
@Slf4j
public class GptRetryableCaller {

    /**
     * GPT API 호출
     */
    @Retryable(
            retryFor = { IOException.class, ServiceException.class },
            interceptor = "retryOperationsInterceptor"
    )
    public OpenAiResponse call(WebClient webClient, String apiKey, String prompt) {
        log.debug("[GptRetryableCaller] 요청할 프롬프트:\n{}", prompt);

        return webClient.post()
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(new OpenAiRequest("gpt-4o", new Message[]{new Message("user", prompt)}))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("[GptRetryableCaller] GPT API 호출 실패. 상태: {}, 응답: {}", clientResponse.statusCode(), body);
                                    return Mono.error(new ServiceException(ErrorCode.GPT_API_CALL_FAILED));
                                })
                )
                .bodyToMono(OpenAiResponse.class)
                .block();
    }

    /**
     * 재시도 실패 후 호출되는 메서드 (최대 재시도 후에도 예외 발생 시 호출)
     * @Recover 어노테이션을 사용하여 재시도 실패 후 처리
     */
    @Recover
    public OpenAiResponse recover(IOException ex, WebClient webClient, String apiKey, String prompt) {
        log.error("[GptRetryableCaller] GPT 호출 재시도 실패: {}", ex.getMessage());
        throw new ServiceException(ErrorCode.GPT_API_CALL_FAILED);
    }
}
