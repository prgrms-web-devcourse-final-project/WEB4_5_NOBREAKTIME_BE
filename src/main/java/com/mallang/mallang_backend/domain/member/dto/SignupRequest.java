package com.mallang.mallang_backend.domain.member.dto;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import jakarta.validation.constraints.NotNull;

/**
 * OAuth 가입 요청 DTO (Record)
 *
 * @param platformId   OAuth 제공자 식별자 (필수)
 * @param email        사용자 이메일 (필수)
 * @param nickname     사용자 닉네임 (필수)
 * @param profileImage 프로필 이미지 URL (필수)
 * @param loginPlatform 로그인 제공자 (필수)
 */
public record SignupRequest(
        @NotNull String platformId,
        @NotNull String email,
        @NotNull String nickname,
        String profileImage,
        @NotNull LoginPlatform loginPlatform
) {}
