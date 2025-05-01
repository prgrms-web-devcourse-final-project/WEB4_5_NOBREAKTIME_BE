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

    /**
     * email 로 memberId 조회
     * @param email (로그인 시 이용하는 ID 값)
     * @return memberId (Long, PK)
     */
    public Long getMemberByEmail (String email) {
        return memberRepository.findByEmail(email).orElseThrow(() ->
                new ServiceException(USER_NOT_FOUND)).getId();
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

    // 소셜 로그인 회원 언어 정보 추가
    @Transactional
    public void updateLearningLanguage(Long id, Language language) {
        Member member = memberRepository.findById(id).orElseThrow(() ->
                new ServiceException(USER_NOT_FOUND));
        member.updateLearningLanguage(language);
    }

    /**
     * member 에 접근해서 구독 정보를 가져 오기
     * @param memberId
     * @return member 의 구독 타입에서 가져온 권한 정보
     */
    public String getSubscription(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new ServiceException(USER_NOT_FOUND));

        return member.getSubscription().getRoleName();
    }
}
