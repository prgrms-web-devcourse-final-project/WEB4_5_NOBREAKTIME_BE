package com.mallang.mallang_backend.global.config.oauth.service;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.mallang.mallang_backend.global.exception.ErrorCode.MEMBER_ALREADY_WITHDRAWN;
import static com.mallang.mallang_backend.global.exception.ErrorCode.MEMBER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncService {

    private final MemberRepository memberRepository;

    @Async("securityTaskExecutor")
    public void handleExistingMember(String platformId) {

        Member member = memberRepository.findByPlatformId(platformId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        if (member.getPlatformId() == null) {
            throw new ServiceException(MEMBER_ALREADY_WITHDRAWN);
        }

        log.info("이미 존재하는 회원: {}", platformId);
    }
}
