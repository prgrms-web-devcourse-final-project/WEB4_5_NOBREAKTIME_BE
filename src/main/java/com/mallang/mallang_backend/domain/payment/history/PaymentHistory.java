package com.mallang.mallang_backend.domain.payment.history;

import com.mallang.mallang_backend.domain.payment.entity.PayStatus;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * 결제 이력/로그 테이블
 * 결제 상태가 변경될 때마다 변경 내역을 기록
 * 언제, 어떤 상태로, 왜 바뀌었는지 문제 추적 용도
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_history_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Payment payment;

    @Column(nullable = false)
    private PayStatus status; // 변경된 상태

    @Column(nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String reasonDetail; // 상태 변경 사유 Enum -> 문자열 변환해 저장 가능

    @Builder
    public PaymentHistory(Payment payment,
                          PayStatus status,
                          LocalDateTime changedAt,
                          String reasonDetail
    ) {
        this.payment = payment;
        this.status = status;
        this.changedAt = changedAt;
        this.reasonDetail = reasonDetail;
    }
}