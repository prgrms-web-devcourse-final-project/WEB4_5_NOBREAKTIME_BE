package com.mallang.mallang_backend.domain.member.service.main;


import com.mallang.mallang_backend.domain.member.dto.ChangeInfoRequest;
import com.mallang.mallang_backend.domain.member.dto.ChangeInfoResponse;
import com.mallang.mallang_backend.domain.member.dto.UserProfileResponse;
import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.global.common.Language;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {

    Member getMemberByPlatformId (String platformId);

    Member getMemberById(Long memberId);

    void updateLearningLanguage(Long id, Language language);

    void withdrawMember(Long memberId);

    String changeProfile(Long memberId, MultipartFile file);

    ChangeInfoResponse changeInformation(Long memberId, ChangeInfoRequest request);

    void deleteOldProfileImage(Long memberId);

    boolean isNicknameAvailable(String nickname);

    UserProfileResponse getUserProfile(Long memberId);

    Boolean existsByPlatformId(String platformId);

    Member findByPlatformId(String platformId);

    boolean existsByNickname(String nickname);

    void signupByOauth(String platformId,
                       String email,
                       String nickname,
                       String profileImage,
                       LoginPlatform loginPlatform);
}
