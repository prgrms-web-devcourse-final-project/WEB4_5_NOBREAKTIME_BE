package com.mallang.mallang_backend.domain.plan.entity.domain.member.service.main;


import com.mallang.mallang_backend.domain.plan.entity.domain.member.dto.ChangeInfoRequest;
import com.mallang.mallang_backend.domain.plan.entity.domain.member.dto.ChangeInfoResponse;
import com.mallang.mallang_backend.domain.plan.entity.domain.member.dto.UserProfileResponse;
import com.mallang.mallang_backend.domain.plan.entity.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.plan.entity.domain.member.entity.Member;
import com.mallang.mallang_backend.global.common.Language;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {

    boolean existsByPlatformId(String platformId);
    Long signupByOauth(String platformId, String email, String nickname, String profileImage, LoginPlatform loginPlatform);
    Member getMemberByPlatformId (String platformId);
    String getRoleName(Long memberId);
    Member getMemberById(Long memberId);
    void updateLearningLanguage(Long id, Language language);
    void withdrawMember(Long memberId);
    void scheduleAccountDeletion();
    String changeProfile(Long memberId, MultipartFile file);
    ChangeInfoResponse changeInformation(Long memberId, ChangeInfoRequest request);

    void deleteOldProfileImage(Long memberId);

    boolean isNicknameAvailable(String nickname);
    void validateEmailNotDuplicated(String email);
    UserProfileResponse getUserProfile(Long memberId);
}
