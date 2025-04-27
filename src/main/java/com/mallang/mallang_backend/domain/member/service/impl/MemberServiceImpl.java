package com.mallang.mallang_backend.domain.member.service.impl;

import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.member.service.MemberService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mallang.mallang_backend.global.common.Language.NONE;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

/**
 * 쓰기 작업(등록, 수정, 삭제 등)은 별도로 @Transactional 붙여 주세요
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    // 이메일로 멤버가 존재하는지 확인
    public Boolean isExistEmail(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

    // 소셜 로그인 회원 멤버 가입
    @Transactional
    public Long signupByOauth(String id, String nickname, String profileImage, LoginPlatform loginPlatform) {

        Member member = Member.builder()
                .email(id)
                .password(null)
                .nickname(nickname)
                .loginPlatform(loginPlatform)
                .language(NONE)
                .profileImageUrl(profileImage).build();

        memberRepository.save(member);

        return member.getId();
    }

    @Override
    public Long getMemberId(String email) {
        return memberRepository.findByEmail(email).get().getId();
    }

    // 소셜 로그인 회원 언어 정보 추가 -> 변경 감지 이용
    public void updateLearningLanguage(Long id, Language language) {
        Member member = memberRepository.findById(id).orElseThrow(() ->
                new ServiceException(USER_NOT_FOUND));
        member.updateLearningLanguage(language);
    }
}
