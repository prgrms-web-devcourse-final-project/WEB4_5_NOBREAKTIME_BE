package com.mallang.mallang_backend.domain.payment.service.process.prepare;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentApproveRequest;
import com.mallang.mallang_backend.domain.payment.entity.PayStatus;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.repository.PaymentQueryRepository;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.payment.service.request.PaymentRedisService;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.plan.repository.PlanRepository;
import com.mallang.mallang_backend.global.config.QueryDslConfig;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.init.factory.EntityTestFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ActiveProfiles("local")
@Import(QueryDslConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // 컨텍스트 재생성
@ExtendWith(OutputCaptureExtension.class)
class PreparePayServiceImplTest {

    @MockitoBean
    private PaymentQueryRepository repository;

    @MockitoBean
    private ApplicationEventPublisher publisherMock;

    @MockitoBean
    private PaymentRedisService redisServiceMock;

    @Autowired
    private PreparePayServiceImpl service;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    EntityTestFactory factory;

    @Autowired
    PlanRepository planRepository;

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        planRepository.deleteAll();
        log.info("clear");
        //reset mocks
        reset(repository);
        reset(publisherMock);
        reset(redisServiceMock);
        log.info("reset");
    }

    @Test
    @Transactional
    @DisplayName("결제 로직 트랜잭션 분리 - 외부 API 호출 전 정보 저장 및 검증")
    void t1() throws Exception {
        //given
        Member member = factory.saveMember();
        Plan plan = planRepository.findById(4L).get();

        Payment payment = factory.saveReadyPayment(member.getId(), plan);
        log.info("이전 결제 상태 확인: {}", paymentRepository.findById(payment.getId()).get().getPayStatus());
        assertThat(paymentRepository.findById(payment.getId()).get().getPayStatus()).isEqualTo(PayStatus.READY);

        doNothing().when(redisServiceMock).checkAndSaveIdempotencyKey(anyString());
        doNothing().when(redisServiceMock).checkOrderIdAndAmount(anyString(), anyInt());
        doNothing().when(publisherMock).publishEvent(any());

        PaymentApproveRequest request = PaymentApproveRequest.builder()
                .idempotencyKey(UUID.randomUUID().toString())
                .paymentKey(UUID.randomUUID().toString())
                .amount(10000)
                .orderId(payment.getOrderId())
                .build();
        //when
        service.preparePayment(request);

        //then: updatePaymentStatus 가 제대로 이루어졌는지 확인
        verify(redisServiceMock, times(1)).checkAndSaveIdempotencyKey(any());
        verify(redisServiceMock, times(1)).checkOrderIdAndAmount(any(), anyInt());
        log.info("결제 상태 확인: {}", paymentRepository.findById(payment.getId()).get().getPayStatus());
        assertThat(paymentRepository.findById(payment.getId()).get().getPayStatus()).isEqualTo(PayStatus.IN_PROGRESS);
    }

    @Test
    @Transactional
    @DisplayName("예외 경우 테스트 - 횟수 안에 성공했을 때에는 예외가 발생하지 않음")
    void t2() throws Exception {
        //given: 2번째 시도에서 성공
        Member member = factory.saveMember();
        Plan plan = planRepository.findById(4L).get();

        Payment payment = factory.saveReadyPayment(member.getId(), plan);

        // 첫 번째 호출: 예외 발생
        // 두 번째 호출부터: 정상 처리
        doThrow(new QueryTimeoutException("재시도 예외 발생"))
                .doNothing()  // 이후 호출 정상 처리
                .when(redisServiceMock).checkOrderIdAndAmount(any(), anyInt());

        PaymentApproveRequest request = PaymentApproveRequest.builder()
                .idempotencyKey(UUID.randomUUID().toString())
                .paymentKey(UUID.randomUUID().toString())
                .amount(10000)
                .orderId(payment.getOrderId())
                .build();

        //when
        service.preparePayment(request);

        //then: 두 번만 접근, 그 이후에는 성공
        verify(redisServiceMock, times(2))
                .checkOrderIdAndAmount(any(), anyInt());
    }

    @Test
    @Disabled("슬랙에 알람이 감...")
    @Transactional
    @DisplayName("예외 경우 테스트 - fallbackMethod 동작 확인")
    void t3(CapturedOutput out) throws Exception {
        //given: 전체 실패
        Member member = factory.saveMember();
        Plan plan = planRepository.findById(4L).get();

        Payment payment = factory.saveReadyPayment(member.getId(), plan);

        doThrow(new QueryTimeoutException("재시도 예외 발생"))
                .when(redisServiceMock).checkOrderIdAndAmount(any(), anyInt());

        PaymentApproveRequest request = PaymentApproveRequest.builder()
                .idempotencyKey(UUID.randomUUID().toString())
                .paymentKey(UUID.randomUUID().toString())
                .amount(10000)
                .orderId(payment.getOrderId())
                .build();

        //when
        assertThrows(ServiceException.class, () -> service.preparePayment(request));

        //then
        verify(redisServiceMock, times(3))
                .checkOrderIdAndAmount(any(), anyInt());

        assertThat(out.getOut())
                .contains("[결제 정보 저장 실패] orderId:");
    }

    @Test
    @Transactional
    @DisplayName("예외 경우 테스트 - 재시도 하지 않는 예외의 경우 바로 오류 처리")
    void t4() throws Exception {
        //given: 2번째 시도에서 성공
        Member member = factory.saveMember();
        Plan plan = planRepository.findById(4L).get();

        Payment payment = factory.saveReadyPayment(member.getId(), plan);

        // 첫 번째 호출: 예외 발생
        // 두 번째 호출부터: 정상 처리
        doThrow(new ServiceException(ORDER_AMOUNT_MISMATCH))
                .doNothing()  // 이후 호출 정상 처리
                .when(redisServiceMock).checkOrderIdAndAmount(any(), anyInt());

        PaymentApproveRequest request = PaymentApproveRequest.builder()
                .idempotencyKey(UUID.randomUUID().toString())
                .paymentKey(UUID.randomUUID().toString())
                .amount(10000)
                .orderId(payment.getOrderId())
                .build();

        //when
        assertThrows(ServiceException.class, () -> service.preparePayment(request));

        //then: 한 번만 접근하고 바로 오류 발생
        verify(redisServiceMock, times(1))
                .checkOrderIdAndAmount(any(), anyInt());
    }
}