package com.mallang.mallang_backend.domain.member.service.profile;

import com.mallang.mallang_backend.domain.member.dto.SubscriptionResponse;
import com.mallang.mallang_backend.domain.member.dto.UserProfileResponse;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.subscription.entity.Subscription;
import com.mallang.mallang_backend.domain.subscription.entity.SubscriptionStatus;
import com.mallang.mallang_backend.domain.subscription.repository.SubscriptionQueryRepository;
import com.mallang.mallang_backend.domain.subscription.repository.SubscriptionRepository;
import com.mallang.mallang_backend.domain.subscription.service.SubscriptionServiceImpl;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.util.s3.S3ImageUploader;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;


@Slf4j
@ExtendWith(MockitoExtension.class)
class MemberProfileServiceImplTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private SubscriptionQueryRepository queryRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private S3ImageUploader imageUploader;
    @InjectMocks
    private MemberProfileServiceImpl memberProfileService;
    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private Clock fixedClock;
    private Member member;
    private Subscription subscription;
    private Plan plan;

    @BeforeEach
    void setUp() {
        memberProfileService = new MemberProfileServiceImpl(imageUploader, memberRepository, subscriptionRepository);

        member = Member.builder()
                .nickname("nick")
                .email("email@example.com")
                .profileImageUrl("profile.jpg")
                .language(Language.ENGLISH)
                .build();

        plan = Plan.builder()
                .type(SubscriptionType.PREMIUM)
                .build();
    }


    @Test
    void getUserProfile_정상조회() {
        // given
        List<Subscription> subscriptions = new ArrayList<>();
        Subscription subscription1 = Subscription.builder()
                .plan(plan)
                .startedAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .expiredAt(LocalDateTime.of(2025, 2, 1, 0, 0))
                .build();

        Subscription subscription2 = Subscription.builder()
                .plan(plan)
                .startedAt(LocalDateTime.of(2025, 2, 1, 0, 0))
                .expiredAt(LocalDateTime.of(2025, 3, 1, 0, 0))
                .build();
        subscription2.updateAutoRenew(true);

        Subscription subscription3 = Subscription.builder()
                .plan(plan)
                .startedAt(LocalDateTime.of(2025, 5, 1, 0, 0))
                .expiredAt(LocalDateTime.of(2025, 6, 1, 0, 0))
                .build();
        subscription3.updateAutoRenew(true);
        subscriptions.add(subscription1);
        subscriptions.add(subscription2);
        subscriptions.add(subscription3);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(subscriptionRepository.findByMember(member)).thenReturn(Optional.of(subscriptions));

        // when
        UserProfileResponse result = memberProfileService.getUserProfile(1L);

        // then
        verify(memberRepository).findById(1L);
        verify(subscriptionRepository).findByMember(member);

        assertThat(result.getNickname()).isEqualTo("nick");
        assertThat(result.getEmail()).isEqualTo("email@example.com");
        assertThat(result.getProfileImage()).isEqualTo("profile.jpg");
        assertThat(result.getSubscriptionType()).isEqualTo(SubscriptionType.BASIC);

        List<SubscriptionResponse> list = result.getSubscriptions();
        log.info("list: {}", list);
        assertThat(list.get(1).getIsPossibleToCancel()).isFalse();
        assertThat(list.getLast().getIsPossibleToCancel()).isTrue();
    }

    @Test
    @DisplayName("만료 시간이 지났을 때 update 를 하면 변경된다")
    void whenExpiredAtIsBeforeNow_updateToExpired() {
        // 2025-05-15 10:00:00 고정
        fixedClock = Clock.fixed(
                Instant.parse("2025-05-15T01:00:00Z"),
                ZoneId.systemDefault()
        );

        Subscription subscription = Subscription.builder()
                .plan(plan)
                .startedAt(LocalDateTime.of(2025, 2, 1, 0, 0))
                .expiredAt(LocalDateTime.of(2025, 3, 1, 0, 0))
                .build();

        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(queryRepository.findLatestByMember(member)).thenReturn(Optional.of(subscription));

        // when
        subscriptionService.updateSubscriptionStatus(1L, fixedClock);

        // then
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
    }

    @Test
    @DisplayName("만료 시간이 지나지 않았을 때 update 를 하면 변경되지 않는다")
    void whenExpiredAtIsBeforeNow_didntUpdateToExpired() throws Exception {
        //given
        // 2025-05-15 10:00:00 고정
        fixedClock = Clock.fixed(
                Instant.parse("2025-05-15T01:00:00Z"),
                ZoneId.systemDefault()
        );

        Subscription subscription = Subscription.builder()
                .plan(plan)
                .startedAt(LocalDateTime.of(2025, 5, 1, 0, 0))
                .expiredAt(LocalDateTime.of(2025, 6, 1, 0, 0))
                .build();

        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(queryRepository.findLatestByMember(member)).thenReturn(Optional.of(subscription));

        // when
        subscriptionService.updateSubscriptionStatus(1L, fixedClock);

        //then
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void getUserProfile_회원없음_예외() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberProfileService.getUserProfile(1L))
                .isInstanceOf(ServiceException.class);
    }
}