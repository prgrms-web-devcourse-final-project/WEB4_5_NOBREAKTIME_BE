package com.mallang.mallang_backend.global.aop.monitor.calaulator;

import com.mallang.mallang_backend.global.dto.TokenUsageType;
import com.mallang.mallang_backend.global.gpt.dto.OpenAiResponse;
import org.springframework.stereotype.Component;

// OpenAI 전용 계산기
@Component
public class OpenAiTokenCalculator implements TokenUsageCalculator {
    @Override
    public double calculate(String apiName,
                            Object result,
                            boolean success,
                            TokenUsageType usageType
    ) {
        if (result instanceof OpenAiResponse response && response.getUsage() != null) {
            return response.getUsage().getTotal_tokens();
        }
        return 0;
    }
}