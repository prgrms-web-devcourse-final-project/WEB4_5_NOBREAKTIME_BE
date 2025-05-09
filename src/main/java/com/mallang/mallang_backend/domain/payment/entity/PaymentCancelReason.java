package com.mallang.mallang_backend.domain.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentCancelReason {

    USER_REQUEST("사용자가 직접 취소 요청"),
    DUPLICATE_PAYMENT("중복 결제"),
    SYSTEM_ERROR("시스템 오류"),
    OTHER("기타 사유");

    private final String description;
}