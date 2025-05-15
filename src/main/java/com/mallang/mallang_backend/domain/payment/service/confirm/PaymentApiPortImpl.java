package com.mallang.mallang_backend.domain.payment.service.confirm;

import com.mallang.mallang_backend.domain.payment.dto.approve.BillingApproveRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.BillingPaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.request.BillingPaymentRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.dto.request.IssueBillingKeyRequest;
import com.mallang.mallang_backend.domain.payment.dto.request.IssueBillingKeyResponse;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;


import java.util.Objects;

import static com.mallang.mallang_backend.global.exception.ErrorCode.API_ERROR;
import static com.mallang.mallang_backend.global.exception.ErrorCode.PAYMENT_CONFIRM_FAIL;

/**
 * Toss Payments API 연동을 처리하는 서비스 구현체입니다.
 *
 * 결제 승인 요청 및 결과 처리를 담당합니다.
 * WebClient를 사용한 동기식 통신 방식을 적용합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApiPortImpl implements PaymentApiPort {

    private final WebClient tossPaymentsSingleWebClient;
    private final WebClient tossPaymentsBillingWebClient;

    /**
     * 토스 페이먼츠 결제 승인 API 호출
     *
     * @param approveRequest 멱등키 (헤더 이용), 결제 금액, 주문 ID, 결제 고유 키
     * @return PaymentResponse 결제 승인 응답
     * @throws ServiceException API 호출 실패 시 발생
     */
    @Override
    public PaymentResponse callTossPaymentAPI(PaymentApproveRequest approveRequest) {

        String idempotencyKey = approveRequest.getPaymentKey();
        int amount = approveRequest.getAmount();
        String paymentKey = approveRequest.getPaymentKey();
        String orderId = approveRequest.getOrderId();

        try {
            PaymentApproveRequest request = buildPaymentRequest(amount, orderId, paymentKey);
            PaymentResponse response = approvePayment(request, idempotencyKey);

            log.info("[결제 승인 성공] orderId: {}, paymentKey: {}",
                    orderId, paymentKey);

            return response;
        } catch (WebClientResponseException e) {
            log.error("[API 호출 실패] orderId: {} | Status: {} | Error: {}",
                    orderId, e.getStatusCode(), e.getResponseBodyAsString());

            throw new ServiceException(PAYMENT_CONFIRM_FAIL, e);
        } catch (Exception e) {
            log.error("[시스템 에러] orderId: {} | Error: {}",
                    orderId, e.getMessage(), e);

            throw new ServiceException(API_ERROR, e);
        }
    }

    /**
     * 결제 승인 요청 객체를 생성합니다.
     *
     * @param amount     결제 금액
     * @param orderId    주문 식별자
     * @param paymentKey 결제 고유 키
     * @return PaymentApproveRequest 결제 승인 요청 객체
     */
    private PaymentApproveRequest buildPaymentRequest(int amount, String orderId, String paymentKey) {
        return PaymentApproveRequest.builder()
                .paymentKey(paymentKey)
                .amount(amount)
                .orderId(orderId)
                .build();
    }

    /**
     * 토스페이먼츠 결제 승인 API를 호출하여 결과를 반환합니다.
     *
     * @param request 결제 승인 요청 객체
     * @return PaymentResponse 결제 승인 응답 객체
     */
    private PaymentResponse approvePayment(PaymentApproveRequest request,
                                           String idempotencyKey
    ) {
        // WebClient를 사용하여 토스페이먼츠 결제 승인 API(/v1/payments/confirm)에 POST 요청을 보냅니다.
        // 요청 본문에는 결제 승인 요청 객체(request)를 전달합니다.
        // 응답은 PaymentResponse 타입으로 역직렬화하여 동기 방식으로(block) 반환합니다.

        return tossPaymentsSingleWebClient.post()
                .uri("/v1/payments/confirm")
                .header("Idempotency-Key", idempotencyKey) // 중복 요청 방지
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .block();
    }

    // ========= 자동 결제 요청 로직 =========

    @Override
    public String callTossPaymentBillingAPI(BillingPaymentRequest approveRequest
    ) {
        log.debug("[빌링 키 발급 프로세스 시작] orderId: {}", approveRequest.getOrderId());

        String customerKey = approveRequest.getCustomerKey();
        String authKey = approveRequest.getAuthKey();

        IssueBillingKeyRequest billingRequest = new IssueBillingKeyRequest(customerKey, authKey);

        try {
            IssueBillingKeyResponse billingResponse = tossPaymentsBillingWebClient.post()
                    .uri("/v1/billing/authorizations/issue")
                    .header("Content-Type", "application/json")
                    .bodyValue(billingRequest)
                    .retrieve()
                    .bodyToMono(IssueBillingKeyResponse.class)
                    .block();

            log.debug("[빌링 키 Response] : {}", billingResponse);
            log.info("[빌링 키 발급 성공] orderId: {}, billingKey: {}",
                    approveRequest.getOrderId(), billingResponse.getBillingKey());

            return Objects.requireNonNull(billingResponse).getBillingKey();
        } catch (WebClientResponseException e) {
            log.error("[빌링 키 발급 실패] orderId: {} | Status: {} | Error: {}",
                    approveRequest.getOrderId(), e.getStatusCode(), e.getResponseBodyAsString());

            throw new ServiceException(PAYMENT_CONFIRM_FAIL, e);
        } catch (Exception e) {
            log.error("[시스템 에러] orderId: {} | Error: {}",
                    approveRequest.getOrderId(), e.getMessage(), e);

            throw new ServiceException(API_ERROR, e);
        }
    }

    // 빌링 키를 이용해서 결제 요청을 전송한 후 응답 객체를 반환
    @Override
    public BillingPaymentResponse payWithBillingKey(String billingKey,
                                                    BillingPaymentRequest request
    ) {
        BillingApproveRequest payRequest = new BillingApproveRequest(
                request.getCustomerKey(),
                request.getAmount(),
                request.getOrderId(),
                request.getOrderName()
        );

        return tossPaymentsBillingWebClient.post()
                .uri("/v1/billing/" + billingKey)
                .header("Content-Type", "application/json")
                .bodyValue(payRequest)
                .retrieve()
                .bodyToMono(BillingPaymentResponse.class)
                .block();
    }



}
