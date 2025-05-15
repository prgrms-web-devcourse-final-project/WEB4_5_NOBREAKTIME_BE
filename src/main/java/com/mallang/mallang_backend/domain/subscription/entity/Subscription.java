package com.mallang.mallang_backend.domain.subscription.entity;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Clock;
import java.time.LocalDateTime;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member; // 구독한 사용자의 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    @Enumerated(EnumType.STRING)
    private Plan plan; // 구독한 플랜에 대한 정보

    @Column(nullable = false)
    private LocalDateTime startedAt; // 구독 시작 날짜

    @Column(nullable = false)
    private LocalDateTime expiredAt; // 구독 만료 날짜

    private LocalDateTime changedAt; // 구독 변경 날짜

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE; // 구독 상태

    private Boolean isAutoRenew = false; // 구독 갱신 여부 판단

    @Builder
    public Subscription(Member member,
                        Plan plan,
                        LocalDateTime startedAt,
                        LocalDateTime expiredAt
    ) {
        this.member = member;
        this.plan = plan;
        this.startedAt = startedAt;
        this.expiredAt = expiredAt;
    }

    public void updateStatus(SubscriptionStatus status) {
        this.status = status;
        this.changedAt = LocalDateTime.now();
    }

    // 구독 여부 판단 필드
    public void updateAutoRenew(Boolean isAutoRenew) {
        this.isAutoRenew = isAutoRenew;
    }

    // 현재 시간이 만료 시간보다 이전이면서, 구독 갱신을 허용해 놓은 경우
    public boolean isPossibleToCancel(Clock clock) {
        return LocalDateTime.now(clock).isBefore(expiredAt) && isAutoRenew;
    }
}
