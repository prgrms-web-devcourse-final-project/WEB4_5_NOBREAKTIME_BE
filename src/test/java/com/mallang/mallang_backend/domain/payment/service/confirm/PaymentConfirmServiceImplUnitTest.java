package com.mallang.mallang_backend.domain.payment.service.confirm;

import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;
import com.mallang.mallang_backend.domain.payment.dto.approve.Receipt;
import com.mallang.mallang_backend.domain.payment.service.request.PaymentRedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentConfirmServiceImplUnitTest {

    @Mock
    PaymentRedisService redisService;
    @Mock
    PaymentApiPort paymentApiPort;

    @InjectMocks
    PaymentConfirmServiceImpl paymentConfirmService;

    @Test
    @DisplayName("올바르게 외부 API를 호출하고 응답을 반환 / 결제 성공/실패에 따라 내부 메서드가 호출되는지, 예외가 잘 발생하는지 확인")
    void t1() throws Exception {
        //given
        String paymentKey = "tgen_20250513185847jTTM8";
        String orderId = "250513-E4jnf-00001";
        String orderName = "스탠다드 1년 구독";
        String approvedAt = "2025-05-13T18:59:04+09:00";
        int amount = 43200;
        String method = "간편결제";
        Receipt receipt = new Receipt("http://test.com");

        PaymentApproveRequest request = PaymentApproveRequest.builder()
                .idempotencyKey(paymentKey)
                .amount(amount)
                .orderId(orderId)
                .paymentKey(paymentKey)
                .build();

        PaymentResponse expected = PaymentResponse.builder()
                .paymentKey(paymentKey)
                .approvedAt(approvedAt)
                .status("DONE")
                .receipt(receipt)
                .totalAmount(amount)
                .orderId(orderId)
                .orderName(orderName)
                .method(method)
                .build();

        //when
        when(paymentApiPort.callTossPaymentAPI(any())).thenReturn(expected);
        PaymentResponse result = paymentConfirmService.sendApproveRequest(request);

        // then
        assertThat(result).isEqualTo(expected);
        verify(redisService).checkOrderIdAndAmount(any(),anyInt());
        verify(paymentApiPort).callTossPaymentAPI(request);
    }

}