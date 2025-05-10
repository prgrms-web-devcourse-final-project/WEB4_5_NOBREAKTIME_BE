package com.mallang.mallang_backend.domain.payment.entity;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * 한 결제 건에 대해 최신 상태와 주요 정보를 포함 (결제 1건당 1 row)
 * 사용자 구독 결제 내역 조회, 정산 등에 활용할 수 있을 것이라고 예상
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan; // 결제한 구독 플랜에 관한 값, Plan 의 period 가 한 달 -> 구독

    // == 결제 정보 ==
    @Column
    private String transactionId; // PG사 거래 ID

    @Column(nullable = false)
    private int totalAmount; // 총 결제 금액

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PayStatus payStatus; // 현재 결제 상태

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PayPlatform platform; // 결제 수단

    // ==== 결제 성공 ====
    @Column
    private LocalDateTime approvedAt; // 결제 승인 시각

    // ==== 결제 실패 ====
    @Column
    @Enumerated(EnumType.STRING)
    private PaymentFailureReason failureReason; // 결제 실패 사유

    // ==== 결제 취소 / 환불 ====
    @Column
    @Enumerated(EnumType.STRING)
    private PaymentCancelReason canceledReason; // 결제 취소 사유

    /**
     * 필수 값만 우선 저장
     * @param member        # 회원
     * @param plan          # 구독
     * @param transactionId # 거래 ID
     * @param platform      # 거래 제공자
     */
    @Builder
    public Payment(Member member,
                   Plan plan,
                   String transactionId,
                   PayPlatform platform
    ) {
        this.member = member;
        this.plan = plan;
        this.transactionId = transactionId;
        this.totalAmount = plan.getAmount();
        this.platform = platform;
        this.payStatus = PayStatus.PENDING; // 최초 상태는 '대기'
    }

    /**
     * 결제가 성공하면,
     * 승인 시각(approvedAt), 결제 상태(payStatus), 결제 플랫폼(platfom) 등 성공 관련 필드를 업데이트
     */
    public void success(String transactionId,
                        LocalDateTime approvedAt) {

        this.transactionId = transactionId;
        this.approvedAt = approvedAt;
        this.payStatus = PayStatus.SUCCESS;
    }

    /**
     * 결제가 실패하면,
     * 실패 사유(failureReason), 결제 상태(payStatus) 등을 업데이트
     */
    public void fail(PaymentFailureReason reason) {

        this.failureReason = reason;
        this.payStatus = PayStatus.FAILED;
    }

    /**
     * 결제가 취소/환불되면,
     * 취소 사유(canceledReason), 결제 상태(payStatus) 등을 업데이트
     */
    public void cancel(PaymentCancelReason reason) {

        this.canceledReason = reason;
        this.payStatus = PayStatus.CANCELED;
    }
}
