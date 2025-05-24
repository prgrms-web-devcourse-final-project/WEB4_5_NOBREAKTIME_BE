package com.mallang.mallang_backend.domain.member.entity;

import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.entity.BaseTime;
import com.mallang.mallang_backend.global.exception.ServiceException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.mallang.mallang_backend.global.exception.ErrorCode.MEMBER_ALREADY_WITHDRAWN;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginPlatform loginPlatform;

    @Column(unique = true)
    private String platformId; // 플랫폼 별 고유 식별자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType subscriptionType = SubscriptionType.BASIC;

    @Column(nullable = false)
    private int wordGoal = 20;

    @Column(nullable = false)
    private int videoGoal = 3;

    @Column(nullable = false)
    private Level wordLevel = Level.NONE;

    @Column(nullable = false)
    private Level expressionLevel = Level.NONE;

    private LocalDateTime withdrawalDate;

    @Column(nullable = false)
    private LocalDateTime measuredAt;

    @Builder
    public Member(
            String platformId,
            String email,
            String nickname,
            String profileImageUrl,
            LoginPlatform loginPlatform,
            Language language
    ) {
        this.platformId = platformId;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.loginPlatform = loginPlatform;
        this.language = language;
        this.measuredAt = LocalDateTime.now();
    }

    // =========== 기본 업데이트 =========== //

    /**
     * 닉네임을 업데이트합니다.
     * <p>
     * 기존 닉네임과 새 닉네임이 다를 경우에만 업데이트합니다.
     * 같은 닉네임일 경우 아무런 동작을 하지 않습니다.
     *
     * @param nickname 새로 설정할 닉네임
     */
    public void updateNickname(String nickname) {
        if (!this.nickname.equals(nickname)) {
            this.nickname = nickname;
        }
    }

    // 언어 선택 업데이트
    public void updateLearningLanguage(Language language) {
        this.language = language;
    }

    // 구독 플랜 업데이트 -> 변경 내역이 같이 않을 때에만 업데이트 할 수 있도록
    public void updateSubscription(SubscriptionType subscriptionType) {
        if (this.subscriptionType == subscriptionType) {
            return;
        }
        this.subscriptionType = subscriptionType;
    }

    public void updateWordGoal(int wordGoal) {
        this.wordGoal = wordGoal;
    }

    public void updateVideoGoal(int videoGoal) {
        this.videoGoal = videoGoal;
    }

    public void updateMeasuredAt(LocalDateTime measuredAt) {
        this.measuredAt = measuredAt;
    }

    public void updateWordLevel(Level wordLevel) {
        this.wordLevel = wordLevel;
    }

    public void updateExpressionLevel(Level expressionLevel) {
        this.expressionLevel = expressionLevel;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateLanguage(Language language) {
        this.language = language;
    }

    // =========== 커스텀 비즈니스 로직 =========== //

    /**
     * 추가 단어장, 표현함을 사용할 수 있는지 여부를 검사한다.
     *
     * @return 추가 단어장, 표현함을 사용할 수 있는지 여부
     */
    public boolean canUseAdditaional() {
        return subscriptionType != SubscriptionType.BASIC;
    }

    /**
     * 구독 타입과 언어를 업데이트하는 메서드입니다.
     * <p>
     * 구독 타입이 변경될 때, PREMIUM 타입으로 변경 시 언어를 ALL로 설정합니다.
     * 동일한 타입으로 변경 요청 시 아무 동작도 하지 않습니다.
     *
     * @param newType 변경할 구독 타입
     */
    public void updateSubTypeAndLanguage(SubscriptionType newType) {
        // 변경 사항이 없으면 바로 반환
        if (this.subscriptionType == newType) {
            return;
        }

        // PREMIUM으로 변경 시 언어를 ALL로 설정
        if (newType == SubscriptionType.PREMIUM) {
            this.language = Language.ALL;
        }

        // 구독 타입 변경
        this.subscriptionType = newType;
    }

    /**
     * 회원 탈퇴 처리 및 개인정보 마스킹을 수행합니다.
     * <p>
     * 최초 1회만 실행 가능하며, 탈퇴 시 다음 작업이 수행됩니다:
     * <ul>
     *   <li>탈퇴 일시 기록</li>
     *   <li>식별 정보 마스킹 처리</li>
     *   <li>구독 정보 초기화</li>
     * </ul>
     *
     * @param uuid 시스템에서 생성한 고유 식별자(마스킹 시 사용)
     * @throws ServiceException 이미 탈퇴 처리된 회원인 경우 발생(MEMBER_ALREADY_WITHDRAWN)
     */
    public void markAsWithdrawn(String uuid) {
        // 중복 탈퇴 요청 검증
        if (this.withdrawalDate != null) {
            throw new ServiceException(MEMBER_ALREADY_WITHDRAWN);
        }

        this.withdrawalDate = LocalDateTime.now();
        maskSensitiveInformation(uuid);
    }

    /**
     * 개인 식별 정보를 마스킹 처리하는 내부 메서드
     * <p>
     * 다음 필드를 변경합니다:
     * <ul>
     *   <li>platformId: "withdrawn_[id]_[uuid]_[timestamp]" 형식</li>
     *   <li>nickname: UUID 값으로 대체</li>
     *   <li>email: "withdrawn_[id]_[uuid]_[timestamp]" 형식</li>
     *   <li>프로필 이미지 URL 삭제</li>
     *   <li>로그인 플랫폼 정보 초기화</li>
     *   <li>구독 타입 초기화</li>
     * </ul>
     *
     * @param uuid 마스킹에 사용할 고유 식별자
     */
    private void maskSensitiveInformation(String uuid) {
        final String timestampSuffix = LocalDateTime.now().toString();

        this.platformId = String.format("withdrawn_%s_%s_%s", this.id, uuid, timestampSuffix);
        this.nickname = uuid; // 재가입 방지를 위한 고유 코드 부여
        this.email = String.format("withdrawn_%s_%s_%s", this.id, uuid, timestampSuffix);
        this.profileImageUrl = null;
        this.loginPlatform = LoginPlatform.NONE;
        this.subscriptionType = SubscriptionType.NONE;
    }
}
