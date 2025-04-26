package com.mallang.mallang_backend.domain.member.entity;

import com.mallang.mallang_backend.global.common.Language;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    private String password;

    @Column(nullable = false)
    private String nickname;

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
}
