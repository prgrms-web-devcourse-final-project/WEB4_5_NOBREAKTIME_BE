package com.mallang.mallang_backend.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.subscription.entity.Subscription;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Clock;
import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@Schema(description = "구독 정보 DTO")
public class SubscriptionResponse {

    @Schema(description = "구독 플랜 이름", example = "PREMIUM", allowableValues = {"STANDARD","PREMIUM"})
    private SubscriptionType planName;

    @Schema(description = "결제 금액(원)", example = "8500")
    private int amount;

    @Schema(description = "구독 시작 날짜", example = "yyyy년 MM월 dd일")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일")
    private LocalDateTime startedAt;

    @Schema(description = "구독 만료 날짜", example = "yyyy년 MM월 dd일")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일")
    private LocalDateTime expiredAt;

    @Schema(description = "취소 가능 여부, 이게 true 이면 구독 갱신 중지가 가능합니다.", example = "true", allowableValues = {"true", "false"})
    private Boolean isPossibleToCancel;

    /**
     * Subscription 엔티티로부터 SubscriptionResponse를 생성하는 팩토리 메서드입니다.
     *
     * @param subscription 변환할 구독 엔티티
     * @param clock 취소 가능 여부 계산에 사용할 Clock 인스턴스
     * @return SubscriptionResponse 인스턴스
     *
     * @throws NullPointerException subscription이 null인 경우
     */
    public static SubscriptionResponse fromSubscription(Subscription subscription,
                                            Clock clock) {
        return SubscriptionResponse.builder()
                .amount(subscription.getPlan().getAmount())
                .planName(subscription.getPlan().getType())
                .startedAt(subscription.getStartedAt())
                .expiredAt(subscription.getExpiredAt())
                .isPossibleToCancel(subscription.isPossibleToCancel(clock))
                .build();
    }
}
