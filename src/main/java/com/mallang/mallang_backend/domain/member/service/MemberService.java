package com.mallang.mallang_backend.domain.member.service;


import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.global.common.Language;

public interface MemberService {

    Boolean isExistEmail(String email);
    Long signupByOauth(String id, String nickname, String profileImage, LoginPlatform loginPlatform);
    Long getMemberByEmail (String email);
    String getSubscription(Long memberId);
    Member getMemberById(Long memberId);
    void updateLearningLanguage(Long id, Language language);
}
