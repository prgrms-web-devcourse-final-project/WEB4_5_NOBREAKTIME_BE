package com.mallang.mallang_backend.domain.payment.quartz.service;

import com.mallang.mallang_backend.domain.payment.dto.approve.BillingPaymentResponse;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.quartz.dto.PaymentDto;
import com.mallang.mallang_backend.domain.payment.quartz.dto.SubscriptionRenewalDto;
import com.mallang.mallang_backend.domain.payment.service.confirm.PaymentConfirmService;
import com.mallang.mallang_backend.domain.payment.thirdparty.PaymentApiPort;
import com.mallang.mallang_backend.domain.subscription.repository.SubscriptionQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 구독 자동 결제를 실행하는 서비스 클래스
 * - 자동 갱신 가능한 구독 목록 조회
 * - 각 구독에 대해 결제 정보 생성 및 결제 시도
 * - 결제 성공 시 구독 정보 갱신
 * - 실패 시 보상 트랜잭션 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoBillingFacade implements AutoBillingService {

    private final SubscriptionQueryRepository queryRepository;
    private final PaymentApiPort apiPort;
    private final PaymentConfirmService paymentConfirmService;
    private final PreparedService preparedService;

    /**
     * 자동 갱신 가능한 모든 구독에 대해 결제 처리 실행
     */
    @Override
    public void executeAutoBilling() {
        List<SubscriptionRenewalDto> autoRenewable = queryRepository.findAutoRenewable();
        log.info("자동 결제 가능한 구독 개수: {}", autoRenewable.size());
        autoRenewable.forEach(this::processSubscriptionAutoBilling);
    }

    /**
     * 구독 정보를 기반으로 자동 결제 프로세스를 수행합니다.
     * <p>
     * 처리 흐름:
     * 1. 최근 결제 정보 조회
     * 2. 자동 결제 가능 여부 확인
     * 3. 신규 결제 생성 및 DB 저장
     * 4. 외부 결제 API 호출
     * 5. 결제 결과 처리
     * 6. 예외 발생 시 실패 처리 보상 트랜잭션 실행
     */
    public void processSubscriptionAutoBilling(SubscriptionRenewalDto sub) {
        PaymentDto previousPayment = preparedService.getPreviousPayment(sub.getMemberId());
        Payment newPayment = preparedService.createNewPaymentAndSave(previousPayment); // 결제 호출 전 DB 저장 (트랜잭션 내부)
        executeBillingProcessWithFailover(sub.getPlanDescription(), newPayment, previousPayment);
    }

    /**
     * 결제 실행 및 예외 처리 래퍼 메서드
     */
    public void executeBillingProcessWithFailover(String description,
                                                   Payment newPayment,
                                                   PaymentDto previousPayment) {
        try {
            // 자동 결제 승인 (외부 API 호출) -> 트랜잭션 밖
            handlePaymentResult(executePaymentApi(newPayment)); // 결제 결과 처리 (트랜잭션 내부)
        } catch (Exception e) {
            preparedService.handleExternalApiFailure(newPayment, previousPayment, e); // 외부 API 호출 실패의 경우
            preparedService.handleDbSaveFailed(description, newPayment, previousPayment, e); // 결제 후 DB 저장 실패의 경우
        }
    }

    /**
     * 외부 결제 API를 호출하여 자동 결제를 수행합니다.
     *
     * @param newPayment 자동 결제에 사용할 결제 정보
     * @return BillingPaymentResponse 결제 API의 응답 객체
     */
    public BillingPaymentResponse executePaymentApi(Payment newPayment) {
        return apiPort.payWithBillingKey(
                newPayment.getBillingKey(),
                newPayment.getCustomerKey(),
                newPayment.getOrderId(),
                newPayment.getPlan().getDescription(),
                newPayment.getTotalAmount()
        );
    }

    /**
     * 결제 응답 결과를 기반으로 구독 갱신 DB 업데이트 및 관련 이벤트를 발행합니다.
     *
     * @param response 외부 결제 API의 응답 객체
     */
    public void handlePaymentResult(BillingPaymentResponse response) {
        paymentConfirmService.processAutoBillingPaymentResult(response);
    }
}
