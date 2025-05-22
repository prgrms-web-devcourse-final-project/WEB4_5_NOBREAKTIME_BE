package com.mallang.mallang_backend.domain.subscription.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class MemberSubscriptionDto {

    private Long memberId;
    private Long subscriptionId;

    @QueryProjection
    public MemberSubscriptionDto(Long memberId, Long subscriptionId) {
        this.memberId = memberId;
        this.subscriptionId = subscriptionId;
    }
}