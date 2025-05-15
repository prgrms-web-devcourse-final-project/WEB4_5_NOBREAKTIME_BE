package com.mallang.mallang_backend.domain.subscription.entity;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.global.entity.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 구독한 사용자의 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan; // 구독한 플랜에 대한 정보

    @Column(nullable = false)
    private LocalDate startedAt; // 구독 시작 날짜

    @Column(nullable = false)
    private LocalDate expiredAt; // 구독 만료 날짜

    private SubscriptionStatus status = SubscriptionStatus.ACTIVE; // 구독 상태

    public Subscription(Member member,
                        Plan plan,
                        LocalDate startedAt,
                        LocalDate expiredAt
    ) {
        this.member = member;
        this.plan = plan;
        this.startedAt = startedAt;
        this.expiredAt = expiredAt;
    }

    public void updateStatus(SubscriptionStatus status) {
        this.status = status;
    }
}
