package com.mallang.mallang_backend.domain.payment.service.confirm;

import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@ExtendWith(OutputCaptureExtension.class)
public class PaymentApiPortImplTest {

    private static MockWebServer mockWebServer;
    private PaymentApiPortImpl paymentApiPort;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }


    @Test
    @DisplayName("결제 세션 만료 에러 테스트")
    void t1(CapturedOutput output) throws Exception {
        //given
        String errorResponse = """
        {
            "code": "NOT_FOUND_PAYMENT_SESSION",
            "message": "결제 시간이 만료되어 결제 진행 데이터가 존재하지 않습니다."
        }
        """;

        // Mock 응답 설정
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(errorResponse)
                .addHeader("Content-Type", "application/json"));


        // WebClient가 Mock 서버를 바라보도록 설정
        WebClient webClient = WebClient.create(mockWebServer.
                url("/v1/payments/confirm").toString());
        paymentApiPort = new PaymentApiPortImpl(webClient);

        //when
        PaymentApproveRequest request = PaymentApproveRequest.builder()
                .paymentKey("paymentKey123")
                .idempotencyKey("idem-1234567890")
                .orderId("order123")
                .amount(10000)
                .build();

        assertThatThrownBy(() ->
                paymentApiPort.callTossPaymentAPI(request))
                .isInstanceOf(ServiceException.class);

        //then
        assertThat(output.getOut())
                .contains("[API 호출 실패]")
                .contains("NOT_FOUND_PAYMENT_SESSION");
    }

    @Test
    @DisplayName("인증되지 않은 키 오류")
    void t2(CapturedOutput output) throws Exception {
        //given
        String errorResponse = """
        {
            "code": "UNAUTHORIZED_KEY",
            "message": "인증되지 않은 시크릿 키 혹은 클라이언트 키 입니다."
        }
        """;

        // Mock 응답 설정
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody(errorResponse)
                .addHeader("Content-Type", "application/json"));


        // WebClient가 Mock 서버를 바라보도록 설정
        WebClient webClient = WebClient.create(mockWebServer.
                url("https://api.tosspayments.com/v1/payments/confirm").toString());
        paymentApiPort = new PaymentApiPortImpl(webClient);

        //when
        PaymentApproveRequest request = PaymentApproveRequest.builder()
                .paymentKey("paymentKey123")
                .idempotencyKey("idem-1234567890")
                .orderId("order123")
                .amount(10000)
                .build();

        assertThatThrownBy(() ->
                paymentApiPort.callTossPaymentAPI(request))
                .isInstanceOf(ServiceException.class);
        //then
        assertThat(output.getOut())
                .contains("[API 호출 실패]")
                .contains("UNAUTHORIZED_KEY");
    }
}
