package com.mallang.mallang_backend.global.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.global.filter.CustomUserDetails;

public class SecurityTestUtils {

	// 사용자 인증 컨텍스트 주입
	public static void authenticateAs(Member member) {
		CustomUserDetails userDetails = new CustomUserDetails(member.getId(), "ROLE_USER");

		UsernamePasswordAuthenticationToken authentication =
			new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
