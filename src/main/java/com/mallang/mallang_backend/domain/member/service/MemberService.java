package com.mallang.mallang_backend.domain.member.service;


import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;

public interface MemberService {

    Boolean isExistEmail(String email);
    Long signupByOauth(String id, String nickname, String profileImage, LoginPlatform loginPlatform);
    Long getMemberId(String email);
}
