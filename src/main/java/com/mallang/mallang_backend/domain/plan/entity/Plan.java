package com.mallang.mallang_backend.domain.plan.entity;

import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.global.entity.BaseTime;
import jakarta.persistence.*;
import lombok.*;

/**
 * 상품 정보를 저장하는 조회용 테이블
 * type + period 로 상품을 선택
 */
@Entity
@Getter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"type", "period"})
        }
)
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionType type; // STANDARD, PREMIUM / 금액

    /**
     * 실제 결제 금액은 Plan 의 price × 기간 × (1 - 할인율)
     * 플랜 + 구독 기간별 계산
     */
    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PlanPeriod period; // // MONTHLY, SIX_MONTHS, YEAR

    private String description;

    @Column(columnDefinition = "TEXT")
    private String benefits; // 제공 혜택에 관한 정보, JSON 문자열 형태

    @Builder
    public Plan(SubscriptionType type,
                int amount,
                PlanPeriod period,
                String description,
                String benefits
    ) {
        this.type = type;
        this.amount = amount;
        this.period = period;
        this.description = description;
        this.benefits = benefits;
    }
}
