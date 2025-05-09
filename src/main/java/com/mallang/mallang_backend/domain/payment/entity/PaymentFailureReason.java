package com.mallang.mallang_backend.domain.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentFailureReason {
    INSUFFICIENT_FUNDS("결제 계좌나 카드에 잔액이 부족하여 결제가 실패"),
    INVALID_CARD("카드 정보(번호, 유효기간, CVC 등)가 잘못되어 결제가 거절"),
    NETWORK_ERROR("결제 과정에서 네트워크 오류(통신 장애, 타임아웃 등)로 인해 결제가 실패"),
    EXPIRED_CARD("카드의 유효기간이 만료되어 결제가 거절"),
    UNKNOWN("알 수 없는 이유로 결제가 실패");

    private final String description;
}