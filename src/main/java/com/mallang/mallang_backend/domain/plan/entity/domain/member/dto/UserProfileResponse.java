package com.mallang.mallang_backend.domain.plan.entity.domain.member.dto;

import com.mallang.mallang_backend.domain.plan.entity.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.global.common.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 전체 정보 조회 응답 DTO")
public class UserProfileResponse {

    @Schema(description = "회원 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "회원 닉네임", example = "test_user")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://s3_버킷_이름/profile.jpg")
    private String profileImage;

    @Schema(description = "구독 정보")
    private SubscriptionType subscriptionType;

    @Schema(description = "언어 설정 정보", example = "ENGLISH")
    private Language language;

    @Schema(description = "구독 내역")
    private List<SubscriptionResponse> subscriptions;
}
