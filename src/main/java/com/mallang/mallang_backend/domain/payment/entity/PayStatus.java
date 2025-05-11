package com.mallang.mallang_backend.domain.payment.entity;

public enum PayStatus {
    PENDING, // PG사 요청 성공 → 승인 대기 중
    SUCCESS, // PG사 승인 완료
    FAILED,  // PG사 승인 거절
    TIMEOUT, // PG사 응답 없음 (일정 시간 초과)
    CANCELED // 결제 취소
}
