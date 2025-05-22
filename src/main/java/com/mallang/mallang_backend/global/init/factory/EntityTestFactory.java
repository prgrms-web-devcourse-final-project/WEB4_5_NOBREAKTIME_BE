package com.mallang.mallang_backend.global.init.factory;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.payment.entity.Payment;
import com.mallang.mallang_backend.domain.payment.repository.PaymentRepository;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.plan.entity.PlanPeriod;
import com.mallang.mallang_backend.domain.plan.repository.PlanRepository;
import com.mallang.mallang_backend.domain.subscription.entity.Subscription;
import com.mallang.mallang_backend.domain.subscription.repository.SubscriptionRepository;
import com.mallang.mallang_backend.global.common.Language;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Transactional
@RequiredArgsConstructor
public class EntityTestFactory {

    private final MemberRepository memberRepository;
    private final PlanRepository planRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    // =============== member entity ============== //
    public static Member createMember() {
        String uuid = UUID.randomUUID().toString();
        return Member.builder()
                .email(uuid + "@test.com")
                .language(Language.ENGLISH)
                .loginPlatform(LoginPlatform.KAKAO)
                .nickname(uuid)
                .platformId(uuid)
                .profileImageUrl("")
                .build();
    }

    public Member saveMember() {
        Member member = createMember();
        return memberRepository.save(member);
    }

    public List<Member> saveMembers(int count) {
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            members.add(createMember());
        }
        return memberRepository.saveAll(members);
    }

    // =============== Payment entity ============== //
    public static Payment createPayment(Long memberId, Plan plan) {
        return Payment.builder()
                .memberId(memberId)
                .plan(plan)
                .orderId(UUID.randomUUID().toString())
                .build();
    }

    public Payment saveReadyPayment(Long memberId, Plan plan) {
        Payment payment = createPayment(memberId, plan);
        return paymentRepository.save(payment);
    }

    public Payment saveSuccessPayment(Long memberId, Plan plan, Clock clock) {
        Payment payment = createPayment(memberId, plan);
        LocalDateTime fixedTime = LocalDateTime.now(clock);
        payment.updateSuccessInfo(UUID.randomUUID().toString(), fixedTime, "결제수단");
        return paymentRepository.save(payment);
    }

    public Payment saveFailurePayment(Long memberId, Plan plan, Clock clock) {
        Payment payment = createPayment(memberId, plan);
        LocalDateTime fixedTime = LocalDateTime.now(clock);
        payment.updateFailureInfo("결제 실패");
        return paymentRepository.save(payment);
    }

    // =============== Plan entity ============== //
    public static Plan createPlan(SubscriptionType type, PlanPeriod period) {
        int amount = type.getBasePrice();
        double discount = period.getDiscountRate();
        int months = period.getMonths();

        int total = (int) ((int) amount * months * discount);
        return Plan.builder()
                .period(period)
                .type(type)
                .amount(total)
                .benefits("")
                .build();
    }

    public Plan savePlan(SubscriptionType type, PlanPeriod period) {
        Plan plan = createPlan(type, period);
        return planRepository.save(plan);
    }

    /**
     * 기본이 ACTIVE -> EXPIRED 가 필요하다면 변경 필요
     */
    public static Subscription createSubscription(Member member,
                                                  Plan plan,
                                                  LocalDateTime startedAt) {

        return Subscription.builder()
                .plan(plan)
                .startedAt(startedAt)
                .member(member)
                .build();
    }

    public Subscription saveSubscription(Member member,
                                          Plan plan,
                                          LocalDateTime startedAt) {
        Subscription subscription = createSubscription(member, plan, startedAt);
        return subscriptionRepository.save(subscription);
    }
}
