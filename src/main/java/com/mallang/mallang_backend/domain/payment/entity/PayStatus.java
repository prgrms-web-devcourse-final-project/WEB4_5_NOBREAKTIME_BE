package com.mallang.mallang_backend.domain.payment.entity;

import lombok.Getter;

@Getter
public enum PayStatus {
    READY("결제 요청 전송 전"),
    IN_PROGRESS("결제수단 정보와 해당 결제수단의 소유자가 맞는지 인증을 마친 상태"),
    WAITING_FOR_DEPOSIT("발급된 가상계좌에 구매자가 아직 입금하지 않은 상태"),
    DONE("인증된 결제수단으로 요청한 결제가 승인된 상태"),
    CANCELED("승인된 결제가 취소된 상태"),
    PARTIAL_CANCELED("승인된 결제가 부분 취소된 상태"),
    ABORTED("결제 승인이 실패한 상태"),
    EXPIRED("결제 유효 시간 30분이 지나 거래가 취소된 상태");
    // IN_PROGRESS 상태에서 결제 승인 API를 호출하지 않으면 EXPIRED가 됨

    private final String description;

    PayStatus(String description) {
        this.description = description;
    }
}
