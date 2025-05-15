package com.mallang.mallang_backend.domain.member.entity;

import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.entity.BaseTime;
import com.mallang.mallang_backend.global.exception.ServiceException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.mallang.mallang_backend.global.exception.ErrorCode.MEMBER_ALREADY_WITHDRAWN;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;


    @Column(unique = true, nullable = false)
    private String email;

    @Column
    private String password;

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
            String password,
            String nickname,
            String profileImageUrl,
            LoginPlatform loginPlatform,
            Language language
    ) {
        this.platformId = platformId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.loginPlatform = loginPlatform;
        this.language = language;
        this.measuredAt = LocalDateTime.now();
    }

    /**
     * 이메일을 업데이트합니다.
     *
     * 기존 이메일과 새 이메일이 다를 때만 업데이트합니다.
     * 같은 닉네임일 경우 아무런 동작을 하지 않습니다.
     * null 상태에서도 새로운 이메일 등록이 가능합니다.
     * - 카카오 회원의 경우 최초 등록일 수 있습니다.
     *
     * @param email 새로 설정할 이메일 주소
     */
    public void updateEmail(String email) {
        if (!Objects.equals(this.email, email)) {
            this.email = email;
        }
    }

    /**
     * 닉네임을 업데이트합니다.
     *
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
        if (this.subscriptionType != subscriptionType) {
            this.subscriptionType = subscriptionType;
        }
    }

    public void updateWordGoal(int wordGoal) {
        this.wordGoal = wordGoal;
    }

    public void updateVideoGoal(int videoGoal) {
        this.videoGoal = videoGoal;
    }

    public void updateWithdrawalDate(LocalDateTime withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
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

    // 회원 탈퇴 후 마스킹 처리
    public void markAsWithdrawn(String uuid) {
        if (this.withdrawalDate != null) {
            throw new ServiceException(MEMBER_ALREADY_WITHDRAWN);
        }
        this.withdrawalDate = LocalDateTime.now();
        maskSensitiveData(uuid);
    }

    private void maskSensitiveData(String uuid) {
        this.platformId = "withdrawn_" + this.id + " " + uuid + " " + LocalDateTime.now();
        this.nickname = uuid; // 고유 랜덤 코드
        this.email = "withdrawn_" + this.id + uuid + " " + LocalDateTime.now();
        this.profileImageUrl = null;
        this.loginPlatform = LoginPlatform.NONE;
        this.subscriptionType = SubscriptionType.NONE;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

}
