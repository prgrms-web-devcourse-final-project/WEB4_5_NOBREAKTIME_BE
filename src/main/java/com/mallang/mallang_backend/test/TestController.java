package com.mallang.mallang_backend.test;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.global.filter.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 로그인 테스트 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
public class TestController {

    private final MemberRepository memberRepository;

    @GetMapping("/api/test")
    public String test(@Login CustomUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        Member findMember = memberRepository.findById(memberId).get();

        return "로그인 성공, 사용자 아이디 값: " + findMember.getId() +
                ", 닉네임: " + findMember.getNickname();

    }
}
