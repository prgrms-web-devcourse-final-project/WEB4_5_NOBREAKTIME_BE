package com.mallang.mallang_backend.global.aop.monitor.calaulator;

import com.mallang.mallang_backend.global.dto.TokenUsageType;

// 토큰 계산 전략 인터페이스
public interface TokenUsageCalculator {
    double calculate(String apiName,
                     Object result,
                     boolean success,
                     TokenUsageType usageType
    );
}