package com.mallang.mallang_backend.global.filter.userfilter;

import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * JWT 인증 유틸리티 클래스
 */

@Slf4j
public class JwtAuthUtils {

    /**
     * claims 에서 사용자 정보를 추출해 인증 객체 설정
     *
     * @param claims -> memberId, roleName이 들어있음
     */
    public static void handleJwt(Claims claims) {
        Long memberId = ((Integer) claims.get("memberId")).longValue();
        String roleName = (String) claims.get("role");
        setAuthentication(memberId, roleName); // 인증 객체 설정
    }

    /**
     * 커스텀 인증 객체 CustomUserDetails 에 Jwt 에서 파싱한 값을 넣어 주기 위한 메서드
     *
     * @param memberId 사용자 고유 id 값 PK
     * @param roleName 사용자 구독 설정에 따른 권한 값
     */
    public static void setAuthentication(Long memberId, String roleName) {
        CustomUserDetails customUserDetails = new CustomUserDetails(memberId, roleName);

        // 인증 완료 후 민감 데이터 제거를 위해 Credentials 필드를 null 로 처리
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("인증 성공: {}", authentication);
    }
}