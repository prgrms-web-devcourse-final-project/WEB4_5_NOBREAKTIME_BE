package com.mallang.mallang_backend.domain.payment.thirdparty;

import com.mallang.mallang_backend.domain.payment.dto.approve.BillingApproveRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.BillingPaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.request.IssueBillingKeyRequest;
import com.mallang.mallang_backend.domain.payment.dto.request.IssueBillingKeyResponse;
import com.mallang.mallang_backend.domain.payment.dto.request.PaymentRequest;
import com.mallang.mallang_backend.domain.payment.service.process.error.HandleErrorService;
import com.mallang.mallang_backend.domain.payment.service.request.PaymentRedisService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Objects;

import static com.mallang.mallang_backend.global.exception.ErrorCode.API_ERROR;

/**
 * Toss Payments API 연동을 처리하는 서비스 구현체입니다.
 * <p>
 * 결제 승인 요청 및 결과 처리를 담당합니다.
 * WebClient를 사용한 동기식 통신 방식을 적용합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApiPortImpl implements PaymentApiPort {

    private final WebClient tossPaymentsSingleWebClient;
    private final WebClient tossPaymentsBillingWebClient;
    private final HandleErrorService errorService;
    private final PaymentRedisService redisService;
    // private final MeterRegistry meterRegistry; -> 이후 추가


    // ========== 공통 WebClient POST 요청 메서드 ==========

    private <T, R> R postForObject(
            WebClient webClient,
            String uri,
            T requestBody,
            Class<R> responseType,
            String idempotencyKey,
            String logPrefix,
            String orderId
    ) {
        R response = webClient.post()
                .uri(uri)
                .header("Content-Type", "application/json")
                .headers(headers -> {
                    if (idempotencyKey != null) {
                        headers.add("Idempotency-Key", idempotencyKey);
                    }
                })
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .block();

        log.info("[{} 성공] orderId: {}", logPrefix, orderId);
        return response;

    }

    // ========== 결제 승인 요청 ==========

    /**
     * 토스 페이먼츠 결제 승인 API 호출
     *
     * @param approveRequest 멱등키 (헤더 이용), 결제 금액, 주문 ID, 결제 고유 키
     * @return PaymentResponse 결제 승인 응답
     * @throws ServiceException API 호출 실패 시 발생
     */
    @Override
    // Retry -> Circuit Breaker 순서로 적용
    @Retry(name = "processPayment", fallbackMethod = "retryFallback")
    @CircuitBreaker(name = "processPayment", fallbackMethod = "circuitBreakerFallback")
    public PaymentResponse callTossPaymentAPI(PaymentApproveRequest approveRequest) {
        String idempotencyKey = approveRequest.getPaymentKey();
        PaymentApproveRequest request = buildPaymentRequest(
                approveRequest.getAmount(),
                approveRequest.getOrderId(),
                approveRequest.getPaymentKey()
        );

        return postForObject(
                tossPaymentsSingleWebClient,
                "/v1/payments/confirm",
                request,
                PaymentResponse.class,
                idempotencyKey,
                "결제 승인",
                approveRequest.getOrderId()
        );
    }

    public PaymentResponse retryFallback(PaymentApproveRequest request,
                                         Throwable t) {

        log.error("[모든 재시도 실패] orderId: {} | Error: {}",
                request.getOrderId(), t.getMessage(), t);
        errorService.handleApiFallback(request.getOrderId()); // 보상 트랜잭션
        throw new ServiceException(API_ERROR, t);
    }

    public PaymentResponse circuitBreakerFallback(PaymentRequest request,
                                                  Throwable t) {
        log.error("[결제 시스템 서킷 OPEN]: {}", t.getMessage());
        throw new ServiceException(API_ERROR, t);
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

    // ========= 자동 결제 요청 로직 ========= //

    // ========== 빌링 키 발급 요청 ========== //

    @Override
    public String issueBillingKey(String customerKey, String authKey, String orderId) {
        log.debug("[빌링 키 발급 프로세스 시작] customerKey: {}", customerKey);

        IssueBillingKeyRequest billingRequest = new IssueBillingKeyRequest(
                customerKey, authKey);

        IssueBillingKeyResponse billingResponse = postForObject(
                tossPaymentsBillingWebClient,
                "/v1/billing/authorizations/issue",
                billingRequest,
                IssueBillingKeyResponse.class,
                null,
                "빌링 키 발급",
                orderId
        );

        log.debug("[빌링 키 Response] : {}", billingResponse);
        return Objects.requireNonNull(billingResponse).getBillingKey();
    }

    // ========== 빌링 결제 요청 ========== //

    @Override
    @CircuitBreaker(
            name = "autoPaymentService",
            fallbackMethod = "payWithBillingKeyFallback"
    )
    @Retry(name = "processPayment", fallbackMethod = "payWithBillingKeyFallback")
    public BillingPaymentResponse payWithBillingKey(String billingKey,
                                                    String customerKey,
                                                    String orderId,
                                                    String orderName,
                                                    int amount) {

        log.debug("[빌링 결제 요청] billingKey: {}, customerKey: {}, orderId: {}, orderName: {}, amount: {}",
                billingKey, customerKey, orderId, orderName, amount);

        String customerOrderIdKey = orderId + "-" + customerKey;
        redisService.checkAndSaveIdempotencyKey(customerOrderIdKey); // 멱등성 키 이용

        BillingApproveRequest payRequest = new BillingApproveRequest(
                customerKey,
                amount,
                orderId,
                orderName
        );

        return postForObject(
                tossPaymentsBillingWebClient,
                "/v1/billing/" + billingKey,
                payRequest,
                BillingPaymentResponse.class,
                null,
                "빌링 결제",
                orderId
        );
    }

    // + 에러 코드로 재시도를 거르는 것도 좋은 방법이라고 생각됨
    public BillingPaymentResponse payWithBillingKeyFallback(String billingKey,
                                                            String customerKey,
                                                            String orderId,
                                                            String orderName,
                                                            int amount,
                                                            Exception e) {
        // 보상 멱등성 키 삭제
        redisService.deleteIdempotencyKey(customerKey, orderId);

        if (e instanceof WebClientResponseException webEx && webEx.getStatusCode().is5xxServerError()) {
            log.error("[빌링결제실패] orderId: {} | Status: {} | Error: {}",
                    orderId, webEx.getStatusCode(), webEx.getResponseBodyAsString());
            // 5xx(서버 장애, 네트워크 문제 등)만 재시도 (RetryableException: 재시도 대상)
            throw new RetryableException("서버 오류, 재시도합니다.", e);
        } else {
            // 4xx(고객 입력 오류, 인증 실패 등)는 재시도해도 의미가 없으니 바로 실패 처리: 보상 작업 실행
            log.error("[구독갱신취소] orderId: {} | Error: {}", orderId, e.getMessage(), e);
            throw new NonRetryableException("결제 실패 - 보상 작업 진행", e);
        } // -> DB 에 FAIL 로 업데이트 / 구독 갱신 [BASIC]
    }

    static class RetryableException extends RuntimeException {
        public RetryableException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class NonRetryableException extends RuntimeException {
        public NonRetryableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
