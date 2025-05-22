package com.mallang.mallang_backend.domain.payment.service.confirm;

import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.approve.Receipt;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.payment.service.request.PaymentRedisService;
import com.mallang.mallang_backend.domain.payment.thirdparty.PaymentApiPort;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.plan.repository.PlanRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.mallang.mallang_backend.global.exception.ErrorCode.PAYMENT_CONFLICT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@Transactional
class PaymentConfirmServiceImplTest {

    @Autowired
    PaymentConfirmServiceImpl paymentConfirmService;

    @Autowired
    PaymentRedisService redisService;

    @Autowired
    PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentApiPort apiPort;

    @Autowired
    PlanRepository planRepository;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.delete(redisTemplate.keys("*")); // 테스트용 데이터 삭제
    }

    @Test
    @Disabled
    @DisplayName("approvePayment 외부 결제 API 응답을 Mock 하여 DB에 저장되는지 검증")
    void testApprovePaymentAndSave() {
        // given: 가짜 응답 세팅 -> 외부 API 호출 x
        String paymentKey = "tgen_20250513185847jTTM8";
        String orderId = "250513-E4jnf-00001";
        String orderName = "스탠다드 1년 구독";
        String approvedAt = "2025-05-13T18:59:04+09:00";
        int amount = 43200;
        String method = "간편결제";
        Receipt receipt = new Receipt("http://test.com");

        // 반환할 가짜 응답 생성
        PaymentResponse fakeResponse = PaymentResponse.builder()
                .paymentKey(paymentKey)
                .approvedAt(approvedAt)
                .status("DONE")
                .receipt(receipt)
                .totalAmount(amount)
                .orderId(orderId)
                .orderName(orderName)
                .method(method)
                .build();

        when(apiPort.callTossPaymentAPI(any())).thenReturn(fakeResponse);

        Plan plan = planRepository.findById(6L).get();

        paymentRepository.save(Payment.builder()
                .memberId(1L)
                .orderId(orderId)
                .plan(plan)
                .build()
        );

        redisService.saveDataToRedis(orderId, amount);

        // 실제 서비스 로직 테스트
        PaymentApproveRequest request = PaymentApproveRequest.builder()
                .idempotencyKey("test123")
                .amount(amount)
                .orderId(orderId)
                .paymentKey(paymentKey)
                .build();

        PaymentResponse response = paymentConfirmService.sendApproveRequest(request);
        paymentConfirmService.processPaymentResult(response);
        // 상위 메서드에서 트랜잭션을 걸어서 이제는 저장이 안 됨

        log.info("response: {}", response); // response: PaymentSuccessResponse(orderId=250513-E4jnf-00001, orderName=스탠다드 1년 구독, status=DONE, approvedAt=2025-05-13T18:59:04+09:00, method=간편결제)

        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();

        assertThat(payment.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getApprovedAt()).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @DisplayName("멱등성 토큰 - 중복 요청 방지 용도")
    void Verify_Duplicate_Request_Prevention_With_Idempotency_Token() {
        // given: 값 저장
        redisService.checkAndSaveIdempotencyKey("dupToken");
        redisService.saveDataToRedis("order789", 20000);

        // when: 검증
        redisService.checkOrderIdAndAmount(
                "order789",
                20000
        );

        // 두 번째 요청 시 예외 발생 검증
        ServiceException exception = assertThrows(ServiceException.class,
                () -> redisService.checkAndSaveIdempotencyKey(
                        "dupToken"
                ));

        assertThat(exception.getErrorCode()).isEqualTo(PAYMENT_CONFLICT);
    }
}