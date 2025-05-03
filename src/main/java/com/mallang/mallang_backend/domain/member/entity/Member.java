package com.mallang.mallang_backend.domain.member.entity;

import java.time.LocalDateTime;

import com.mallang.mallang_backend.global.common.Language;

import com.mallang.mallang_backend.global.exception.ServiceException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private LocalDateTime withdrawalDate;

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

    // 단어장 추가 위해 구독 플랜 확인
    public boolean canCreateWordBook() {
        return subscription != Subscription.BASIC;
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
}
