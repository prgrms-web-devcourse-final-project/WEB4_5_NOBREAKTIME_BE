package com.mallang.mallang_backend.domain.member.service.main;


import com.mallang.mallang_backend.domain.member.dto.ChangeInfoRequest;
import com.mallang.mallang_backend.domain.member.dto.ChangeInfoResponse;
import com.mallang.mallang_backend.domain.member.dto.SignupRequest;
import com.mallang.mallang_backend.domain.member.dto.UserProfileResponse;
import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.global.common.Language;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {

    Member getMemberById(Long memberId);

    void updateLearningLanguage(Long id, Language language);

    void withdrawMember(Long memberId);

    String changeProfile(Long memberId, MultipartFile file);

    ChangeInfoResponse changeInformation(Long memberId, ChangeInfoRequest request);

    void deleteOldProfileImage(Long memberId);

    boolean isNicknameAvailable(String nickname);

    UserProfileResponse getUserProfile(Long memberId);

    boolean existsByNickname(String nickname);

    void signupByOauth(SignupRequest request);
}
