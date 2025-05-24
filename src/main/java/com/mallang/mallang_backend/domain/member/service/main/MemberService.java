package com.mallang.mallang_backend.domain.member.service.main;


import com.mallang.mallang_backend.domain.member.dto.ChangeInfoRequest;
import com.mallang.mallang_backend.domain.member.dto.ChangeInfoResponse;
import com.mallang.mallang_backend.domain.member.dto.UserProfileResponse;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.global.common.Language;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {

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

    UserProfileResponse getUserProfile(Long memberId);
}
