package com.mallang.mallang_backend.domain.member.entity;

import com.mallang.mallang_backend.global.common.Language;
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
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column
    private String profileImageUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginPlatform loginPlatform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Subscription subscription = Subscription.BASIC;

    @Column(nullable = false)
    private LocalDateTime subscribedAt = createdAt;

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
    private LocalDateTime measuredAt = createdAt;

    @Builder
    public Member(
            String email,
            String password,
            String nickname,
            String profileImageUrl,
            LoginPlatform loginPlatform,
            Language language
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.loginPlatform = loginPlatform;
        this.language = language;
    }

    // 언어 선택 업데이트 로직 -> 소셜 로그인 회원
    public void updateLearningLanguage(Language language) {
        this.language = language;
    }

    // 구독 플랜 업데이트
    public void updateSubscription(Subscription subscription) {
        this.subscription = subscription;
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
    public void markAsWithdrawn() {
        if (this.withdrawalDate != null) {
            throw new ServiceException(MEMBER_ALREADY_WITHDRAWN);
        }
        this.withdrawalDate = LocalDateTime.now();
        maskSensitiveData();
    }

    private void maskSensitiveData() {
        this.nickname = "탈퇴회원-" + this.id;
        this.email = "withdrawn_" + this.id;
        this.profileImageUrl = "delete";
        this.loginPlatform = LoginPlatform.NONE;
        this.subscription = Subscription.NONE;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isFirstLevelMeasure() {
        return getWordLevel().equals(Level.NONE) || getExpressionLevel().equals(Level.NONE);
    }
}
