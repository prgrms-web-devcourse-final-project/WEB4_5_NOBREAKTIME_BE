package com.mallang.mallang_backend.global.interceptor;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class SentryUserInterceptor implements HandlerInterceptor {
    private final MemberRepository memberRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 현재 인증된 사용자 정보를 SecurityContextHoler 에서 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 로그인된 사용자 CustomUserDetails 라면
        if (authentication != null && authentication.isAuthenticated() &&  authentication.getPrincipal() instanceof CustomUserDetails userDetails) {

            // 로그인한 사용자의 memberId 추출
            Long memberId = userDetails.getMemberId();

            // 회원 정보 조회
            Member member = memberRepository.findById(memberId).orElse(null);

            // 회원이 존재하면 Sentry 사용자 정보 설정
            if (member != null) {
                User user = new User();
                user.setId(member.getId().toString()); // Sentry 에서 사용자 Id 표시
                user.setEmail(member.getEmail());      // Sentry 에서 사용자 이메일 표시
                user.setUsername(member.getNickname()); // Sentry 에서 사용자 닉네임 표시

                Sentry.setUser(user);

                Sentry.setTag("subscription", member.getSubscriptionType().name());
            }
        }
        return true;
    }
}
