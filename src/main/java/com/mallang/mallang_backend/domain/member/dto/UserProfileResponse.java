package com.mallang.mallang_backend.domain.member.dto;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
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

    @Schema(description = "기본 언어 설정 정보", example = "ENGLISH")
    private Language language;

    @Schema(description = "사용 가능한 언어 설정 정보", example = "ENGLISH, JAPANESE")
    private List<Language> availableLanguages;

    @Schema(description = "구독 내역")
    private List<SubscriptionResponse> subscriptions;

    /**
     * Member 엔티티로부터 프로필 응답을 생성하는 팩토리 메서드
     *
     * @param member 프로필 정보를 가져올 회원 엔티티
     * @param availableLanguages 선택 가능한 언어 목록
     * @param subscriptions 구독 정보 목록
     * @return 완성된 UserProfileResponse 인스턴스
     *
     * @example
     * UserProfileResponse.fromMember(
     *     member,
     *     languages,
     *     subscriptions
     * )
     */
    public static UserProfileResponse fromMember(
            Member member,
            List<Language> availableLanguages,
            List<SubscriptionResponse> subscriptions
    ) {
        return UserProfileResponse.builder()
                .nickname(member.getNickname())
                .email(member.getEmail())
                .profileImage(member.getProfileImageUrl())
                .subscriptionType(member.getSubscriptionType())
                .language(member.getLanguage())
                .availableLanguages(availableLanguages)
                .subscriptions(subscriptions)
                .build();
    }
}
