package com.mallang.mallang_backend.domain.payment.service.request;

import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.payment.dto.request.PaymentRequest;
import com.mallang.mallang_backend.domain.payment.dto.request.PaymentSimpleRequest;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.payment.event.dto.PaymentUpdatedEvent;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.plan.entity.PlanPeriod;
import com.mallang.mallang_backend.domain.plan.repository.PlanRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mallang.mallang_backend.domain.payment.entity.PayStatus.READY;
import static com.mallang.mallang_backend.global.constants.AppConstants.CHARACTERS;
import static com.mallang.mallang_backend.global.constants.AppConstants.DATE_FORMATTER;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentRequestServiceImpl implements PaymentRequestService {

    private final PaymentRepository paymentRepository;
    private final PlanRepository planRepository;
    private final PaymentRedisService redisService;
    private final ApplicationEventPublisher publisher;

    /**
     * 결제 요청을 생성합니다.
     *
     * @param memberId      결제 요청을 하는 회원의 ID
     * @param simpleRequest 결제 요청 객체를 만들 간단한 정보
     * @return 결제 요청 응답
     */
    public PaymentRequest createPaymentRequest(Long memberId,
                                               PaymentSimpleRequest simpleRequest) {

        String orderId = generatedOrderId(memberId);
        Plan selectedPlan = getSelectedPlan(simpleRequest);
        log.debug("Selected plan: {}", selectedPlan.getDescription());

        if (selectedPlan == null) {
            throw new ServiceException(PLAN_NOT_FOUND); // DB에 저장이 되지 않은 상황을 방지하기 위해서
        }

        // redis 저장 이후에 DB 업데이트, redis 저장 실패 -> 결제 요청 취소
        redisService.saveDataToRedis(orderId, selectedPlan.getAmount());

        Long paymentId = createPaymentIfNotExists(
                memberId,
                selectedPlan,
                orderId
        );

        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() ->
                new ServiceException(PAYMENT_NOT_FOUND));

        // 결제 요청 로그 생성 이벤트 발생
        publisher.publishEvent(new PaymentUpdatedEvent(payment.getId(), READY, "결제 요청 생성"));

        return PaymentRequest.from(payment);
    }

    /**
     * 결제 요청에 맞는 플랜을 조회합니다.
     *
     * @param simpleRequest 결제 요청 객체
     * @return 조회된 플랜
     * @throws ServiceException 해당 조건에 맞는 플랜이 없을 경우 발생
     */
    private Plan getSelectedPlan(PaymentSimpleRequest simpleRequest) {
        PlanPeriod period = simpleRequest.getPeriod();
        SubscriptionType type = simpleRequest.getType();

        return planRepository.findByPeriodAndType(period, type);
    }

    /**
     * 주어진 회원 ID를 기반으로 주문 ID를 생성합니다.
     *
     * 주문 ID는 "yyMMdd-랜덤5자리-ID" 형식으로 생성됩니다.
     *
     * @param memberId 주문을 생성한 회원의 ID
     * @return 생성된 주문 ID 문자열
     */
    @Override
    public String generatedOrderId(Long memberId) {
        String orderDate = LocalDate.now().format(DATE_FORMATTER);
        String randomPart = generateRandomString();
        String formattedMemberId = String.format("%05d", memberId); // 5자리 고정

        return String.format("%s-%s-%s", orderDate, randomPart, formattedMemberId);
    }

    /**
     * 다섯 글자의 랜덤 문자열을 생성합니다.
     *
     * @return 랜덤 문자열
     */
    private static String generateRandomString() {
        SecureRandom random = new SecureRandom();
        return IntStream.range(0, 5)
                .mapToObj(i -> String.valueOf(CHARACTERS.charAt(random.nextInt(CHARACTERS.length()))))
                .collect(Collectors.joining());
    }

    /**
     * 주문 ID가 존재할 경우 예외를 발생시킵니다.
     * 주문 ID가 존재하지 않을 때만 결제 데이터를 생성합니다.
     *
     * @param memberId     회원 ID
     * @param selectedPlan 선택된 플랜
     * @param orderId      주문 ID (고유해야 함)
     * @throws ServiceException 이미 동일한 주문 ID가 존재할 경우
     */
    private Long createPaymentIfNotExists(Long memberId,
                                          Plan selectedPlan,
                                          String orderId) {

        // TODO LOCK 적용 필수
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new ServiceException(PAYMENT_CONFLICT);
        }

        return createNewPayment(memberId, selectedPlan, orderId);
    }

    private Long createNewPayment(Long memberId,
                                  Plan selectedPlan,
                                  String orderId) {

        Payment payment = Payment.builder()
                .memberId(memberId)
                .plan(selectedPlan)
                .orderId(orderId)
                .build();

        log.info("Payment created: {}", payment);
        return paymentRepository.save(payment).getId(); // 오류 핸들러에서 저장 오류 처리
    }

    // @Scheduled(fixedDelay = 30000) // 30초마다 실행
    public void checkPendingPayment() {
        // TODO 결제가 요청된 후에 10 분 안에 응답이 오지 않았을 때에는 결제 상태를 PENDING -> FAIL 변경
        // 토스 페이: 결제 인증이 유효한 10분 안에 상점에서 결제 승인 API를 호출하지 않으면 해당 결제는 만료됩니다.
    }
}

