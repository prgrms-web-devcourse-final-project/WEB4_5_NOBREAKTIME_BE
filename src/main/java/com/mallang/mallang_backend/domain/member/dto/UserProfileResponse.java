package com.mallang.mallang_backend.domain.member.dto;

import com.mallang.mallang_backend.domain.member.entity.Subscription;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
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
    private Subscription subscription;
}
