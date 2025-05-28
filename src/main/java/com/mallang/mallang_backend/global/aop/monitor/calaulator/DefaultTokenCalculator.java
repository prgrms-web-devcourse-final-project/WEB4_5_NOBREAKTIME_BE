package com.mallang.mallang_backend.global.aop.monitor.calaulator;

import com.mallang.mallang_backend.global.dto.TokenUsageType;
import org.springframework.stereotype.Component;

// 기본 구현체
@Component
public class DefaultTokenCalculator implements TokenUsageCalculator {
    @Override
    public double calculate(String apiName,
                            Object result,
                            boolean success,
                            TokenUsageType usageType
    ) {
        double cost = usageType.getCost();

        // 실패 시 패널티 추가 계산
        if (!success) {
            cost += TokenUsageType.FAILURE_PENALTY.getCost();
        }
        return cost;
    }
}