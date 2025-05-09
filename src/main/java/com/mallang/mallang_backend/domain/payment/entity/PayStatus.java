package com.mallang.mallang_backend.domain.payment.entity;

public enum PayStatus {
    PENDING, // 결제 대기
    SUCCESS, // 결제 성공
    FAILED,  // 결제 실패
    CANCELED // 결제 취소
}
