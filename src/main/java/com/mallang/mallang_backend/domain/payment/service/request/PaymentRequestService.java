package com.mallang.mallang_backend.domain.payment.service.request;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.payment.dto.PaymentRequest;
import com.mallang.mallang_backend.domain.payment.dto.PaymentSimpleRequest;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.plan.entity.PlanPeriod;
import com.mallang.mallang_backend.domain.plan.repository.PlanRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentRequestService {

    @Value("${toss.payment.successURL}")
    private String successURL;

    @Value("${toss.payment.failURL}")
    private String failURL;

    private final PaymentRepository paymentRepository;
    private final PlanRepository planRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PaymentRedisService redisService;

    /**
     * 결제 요청을 생성합니다.
     *
     * @param memberId      결제 요청을 하는 회원의 ID
     * @param simpleRequest 결제 요청 객체를 만들 간단한 정보
     * @return 결제 요청 응답
     */
    public PaymentRequest createPaymentRequest(String idempotencyKey,
                                               Long memberId,
                                               PaymentSimpleRequest simpleRequest) {

        String orderId = generatedOrderId(memberId);
        Plan selectedPlan = getSelectedPlan(simpleRequest);
        log.debug("Selected plan: {}", selectedPlan.getDescription());

        if (selectedPlan == null) {
            throw new ServiceException(PLAN_NOT_FOUND); // DB에 저장이 되지 않은 상황을 방지하기 위해서
        }

        // 멱등성 토큰 검증
        if (redisTemplate.hasKey(IDEM_KEY_PREFIX + idempotencyKey)) {
            log.warn("동일 주문에 대한 중복 요청 전송");
            throw new ServiceException(ORDER_ID_CONFLICT); // 중복 요청 차단
        }

        // redis 저장 이후에 DB 업데이트
        // redis 저장 실패 -> 결제 요청 취소
        redisService.saveDataToRedis(idempotencyKey, orderId, selectedPlan.getAmount());

        Long paymentId = createPaymentIfNotExists(
                memberId,
                selectedPlan,
                orderId
        );

        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() ->
                new ServiceException(PAYMENT_NOT_FOUND));

        return PaymentRequest.from(payment, successURL, failURL);
    }

    private Plan getSelectedPlan(PaymentSimpleRequest simpleRequest) {
        PlanPeriod period = simpleRequest.getPeriod();
        SubscriptionType type = simpleRequest.getType();

        return planRepository.findByPeriodAndType(period, type);
    }

    /**
     * 주어진 회원 ID를 기반으로 주문 ID를 생성합니다.
     * <p>
     * 주문 ID는 "yyMMdd-랜덤5자리-ID" 형식으로 생성됩니다.
     *
     * @param memberId 주문을 생성한 회원의 ID
     * @return 생성된 주문 ID 문자열
     */
    private String generatedOrderId(Long memberId) {
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
    @Transactional
    public Long createPaymentIfNotExists(Long memberId,
                                         Plan selectedPlan,
                                         String orderId) {

        // TODO LOCK 적용 필수
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new ServiceException(ORDER_ID_CONFLICT);
        } // 조회와 저장을 하나의 트랜잭션으로 -> 동시성 보장 / AOP 미적용 문제 해결

        return createNewPayment(memberId, selectedPlan, orderId);
    }

    private Long createNewPayment(Long memberId,
                                  Plan selectedPlan,
                                  String orderId) {

        Member orderedMember = memberRepository.findById(memberId).orElseThrow(() ->
                new ServiceException(MEMBER_NOT_FOUND));

        Payment payment = Payment.builder()
                .member(orderedMember)
                .plan(selectedPlan)
                .orderId(orderId)
                .build();

        log.info("Payment created: {}", payment);
        return paymentRepository.save(payment).getId(); // 오류 핸들러에서 저장 오류 처리
    }

    @Scheduled(fixedDelay = 30000) // 30초마다 실행
    public void checkPendingPayment() {
        // TODO 결제가 요청된 후에 응답이 오지 않았을 때에는 결제 상태를 PENDING -> FAIL 변경
    }
}

