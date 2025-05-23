package com.mallang.mallang_backend.domain.payment.entity;

import com.mallang.mallang_backend.domain.plan.entity.Plan;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private Long memberId; // 회원 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan; // 결제한 구독 플랜에 관한 값, Plan 의 period 가 한 달 -> 구독

    // == 결제 정보 ==
    @Column(nullable = false, unique = true)
    private String orderId; // 결제 요청 시 전송하는 값 (자동 결제 - customerKey 로 이용)

    @Column(unique = true)
    private String paymentKey; // 각 결제를 식별하는 값 (PG사 제공) -> 취소 시에도 이용

    private String billingKey; // 자동 결제 시에 이용하는 값

    private String customerKey; // 자동 결제 시에 이용하는 값

    @Column(nullable = false)
    private int totalAmount; // 총 결제 금액

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PayStatus payStatus; // 현재 결제 상태

    private String method; // 결제 방법 (카드, 휴대폰, 앱, 웹)

    // ==== 결제 성공 ====
    @Column
    private LocalDateTime approvedAt; // 결제 승인 시각

    // ==== 결제 실패 ====
    @Column
    private String failureReason; // 결제 실패 사유

    /**
     * 필수 값만 우선 저장
     *
     * @param memberId # 회원 ID
     * @param plan     # 구독
     * @param orderId  # 거래 ID
     */
    @Builder
    public Payment(Long memberId,
                   Plan plan,
                   String orderId
    ) {
        this.memberId = memberId;
        this.plan = plan;
        this.totalAmount = plan.getAmount();
        this.orderId = orderId;
        this.payStatus = PayStatus.READY; // 최초 상태는 '대기'
    }

    /**
     * 결제가 성공하면,
     * 승인 시각(approvedAt), 결제 상태(payStatus), 결제 플랫폼(platfom) 등 성공 관련 필드를 업데이트
     */
    public void updateSuccessInfo(String paymentKey,
                                  LocalDateTime approvedAt,
                                  String method) {

        this.paymentKey = paymentKey;
        this.approvedAt = approvedAt;
        this.method = method;
        this.payStatus = PayStatus.DONE;
    }

    public void updateFailureInfo(String reason) {
        this.failureReason = reason;
        this.payStatus = PayStatus.FAILED;
    }

    public void updateStatus(PayStatus status) {
        this.payStatus = status;
    }

    // 자동 결제 카드 승인 완료 후
    public void updateBillingKeyAndCustomerKey(String billingKey, String customerKey) {
        this.billingKey = billingKey;
        this.customerKey = customerKey;
    }

    // 자동 결제가 성공했을 때
    public void updateBillingSuccessInfo(LocalDateTime approvedAt,
                                         String method,
                                         String paymentKey
    ) {
        this.approvedAt = approvedAt;
        this.method = method;
        this.paymentKey = paymentKey;
        this.payStatus = PayStatus.AUTO_BILLING_APPROVED;
    }
}