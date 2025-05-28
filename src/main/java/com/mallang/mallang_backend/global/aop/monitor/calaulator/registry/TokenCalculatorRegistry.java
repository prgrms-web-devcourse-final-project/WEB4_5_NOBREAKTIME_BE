package com.mallang.mallang_backend.global.aop.monitor.calaulator.registry;

import com.mallang.mallang_backend.global.aop.monitor.calaulator.DefaultTokenCalculator;
import com.mallang.mallang_backend.global.aop.monitor.calaulator.OpenAiTokenCalculator;
import com.mallang.mallang_backend.global.aop.monitor.calaulator.TokenUsageCalculator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TokenCalculatorRegistry {
    private final Map<String, TokenUsageCalculator> calculators = new HashMap<>();

    public TokenCalculatorRegistry(
            OpenAiTokenCalculator openAiCalculator,
            DefaultTokenCalculator defaultCalculator
    ) {
        calculators.put("openai", openAiCalculator);
        calculators.put("default", defaultCalculator);
    }

    public TokenUsageCalculator getCalculator(String apiName) {
        return calculators.getOrDefault(
                apiName.toLowerCase(),
                calculators.get("default")
        );
    }
}
