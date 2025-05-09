package com.mallang.mallang_backend.domain.plan.entity;

import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionType type; // STANDARD, PREMIUM / 금액

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PlanPeriod period; // // MONTHLY, SIX_MONTHS, YEAR

    @Column(nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String benefits; // 제공 혜택에 관한 정보, JSON 문자열 형태

    // 플랜 + 구독 기간별 계산

    /**
     * 실제 결제 금액은 Plan 의 price × 기간 × (1 - 할인율)
     * 플랜 + 구독 기간별 계산
     * @return 총 금액
     */
    public int updateTotalAmount() {
        double total = type.getBasePrice() * period.getMonths() * (1 - period.getDiscountRate());
        return (int) Math.round(total);
    }
}
