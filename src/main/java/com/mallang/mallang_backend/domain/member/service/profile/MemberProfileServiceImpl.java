package com.mallang.mallang_backend.domain.member.service.profile;


import com.mallang.mallang_backend.domain.member.dto.ImageUploadRequest;
import com.mallang.mallang_backend.domain.member.dto.SubscriptionResponse;
import com.mallang.mallang_backend.domain.member.dto.UserProfileResponse;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.subscription.entity.Subscription;
import com.mallang.mallang_backend.domain.subscription.repository.SubscriptionRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.util.s3.S3ImageUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.util.Collections;
import java.util.List;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProfileServiceImpl implements MemberProfileService {

    private final S3ImageUploader imageUploader;
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * 주어진 회원 ID로 사용자 프로필 정보를 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return UserProfileResponse 사용자 프로필 응답 DTO
     * @throws ServiceException 회원이 존재하지 않을 경우 발생
     */
    @Override
    public UserProfileResponse getUserProfile(Long memberId) {
        Member member = findMemberOrThrow(memberId);

        List<Subscription> subscriptions = subscriptionRepository.findByMember(member)
                .orElse(Collections.emptyList());

        List<SubscriptionResponse> subscriptionResponses = toSubscriptionResponses(subscriptions);

        return UserProfileResponse.builder()
                .nickname(member.getNickname())
                .email(member.getEmail())
                .profileImage(member.getProfileImageUrl())
                .subscriptionType(member.getSubscriptionType())
                .language(member.getLanguage())
                .subscriptions(subscriptionResponses)
                .build();
    }

    // 구독 리스트를 DTO 리스트로 변환하는 메서드
    private List<SubscriptionResponse> toSubscriptionResponses(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .map(this::toSubscriptionResponse)
                .toList();
    }

    // 구독 엔티티를 DTO로 변환하는 메서드
    private SubscriptionResponse toSubscriptionResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .amount(subscription.getPlan().getAmount())
                .planName(subscription.getPlan().getType())
                .startedAt(subscription.getStartedAt())
                .expiredAt(subscription.getExpiredAt())
                .isPossibleToCancel(subscription.isPossibleToCancel(Clock.systemDefaultZone()))
                .build();
    }

    /**
     * 회원의 프로필 이미지를 변경합니다.
     *
     * @param memberId 프로필을 변경할 회원의 ID
     * @param file     새로 업로드할 프로필 이미지 파일
     * @return 변경된 프로필 이미지의 URL
     * @throws ServiceException 파일이 비어있거나 지원하지 않는 타입일 경우, 회원이 존재하지 않을 경우 발생
     */
    @Override
    @Transactional
    public String changeProfile(Long memberId,
                                MultipartFile file) {

        validateImageFile(file);

        Member member = findMemberOrThrow(memberId);

        deleteOldProfileImage(memberId);

        String newImageUrl = uploadNewProfileImage(file);
        member.updateProfileImageUrl(newImageUrl);

        return newImageUrl;
    }

    /**
     * 이미지 파일의 유효성을 검사합니다.
     *
     * @param file 업로드된 파일
     * @throws ServiceException 파일이 비어있거나 지원하지 않는 타입일 경우 발생
     */
    private void validateImageFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new ServiceException(FILE_EMPTY);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ServiceException(NOT_SUPPORTED_TYPE);
        }
    }

    /**
     * 기존 프로필 이미지를 삭제합니다.
     * - 단순히 외부 저장소에서의 삭제
     *
     * @param memberId 조회할 회원의 ID
     */
    public void deleteOldProfileImage(Long memberId) {

        Member member = findMemberOrThrow(memberId);

        String oldProfileUrl = member.getProfileImageUrl();
        if (oldProfileUrl != null && !oldProfileUrl.isBlank()) {
            imageUploader.deleteObjectByUrl(oldProfileUrl);
        }
    }

    /**
     * 새 이미지를 업로드하고 URL을 반환합니다.
     *
     * @param file 업로드할 이미지 파일
     * @return 업로드된 이미지의 URL
     */
    private String uploadNewProfileImage(MultipartFile file) {
        ImageUploadRequest request = new ImageUploadRequest(file);
        return imageUploader.uploadImageURL(request);
    }

    /**
     * 회원을 조회하고, 없으면 예외를 발생
     *
     * @param memberId 회원 ID
     * @return 회원 엔티티
     */
    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));
    }
}
