package com.mallang.mallang_backend.global.dto;

public enum TokenUsageType {
    NONE(0), // 별도의 외부 API 기본값
    REQUEST(100),        // 호출 자체에 대한 사용
    FAILURE_PENALTY(100),// 실패했을 때만 차감
    FETCH_REQUEST(1);    // fetchVideosByIds 용도

    private final int cost;
    TokenUsageType(int cost) {
        this.cost = cost;
    }

    public int getCost() {
        return cost;
    }
}
