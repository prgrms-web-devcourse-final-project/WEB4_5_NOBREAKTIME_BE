package com.mallang.mallang_backend.domain.plan.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlanPeriod {
    MONTHLY(1, "1개월", 0.0),
    SIX_MONTHS(6, "6개월", 0.1),
    YEAR(12, "1년", 0.2);

    private final int months; // 실제 구독 달수
    private final String displayName; // 사용자에게 보여 줄 한글명
    private final double discountRate; // 할인율
}
