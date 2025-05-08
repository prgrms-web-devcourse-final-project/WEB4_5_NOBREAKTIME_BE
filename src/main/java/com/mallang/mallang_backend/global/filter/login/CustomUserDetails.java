package com.mallang.mallang_backend.global.filter.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


/**
 * 인증된 회원의 상세 정보
 *
 * - memberId: 회원 고유 식별자
 * - roleName: 회원 권한 (예: ROLE_BASIC, ROLE_STANDARD 등)
 */
@Getter
@Setter
@AllArgsConstructor
@Schema(description = "인증된 회원의 상세 정보")
public class CustomUserDetails implements UserDetails {

    @Schema(description = "회원 고유 식별자", example = "12345")
    private Long memberId;

    @Schema(description = "회원 권한", example = "ROLE_STANDARD")
    private String roleName; // ROLE_BASIC, ROLE_STANDARD 등...

    /**
     * roleName 기반으로 권한별 접근 제어하기 위한 메서드
     * 이후 @PreAuthorize("hasRole('BASIC')") 와 같은 형식으로 이용 가능
     * [ROLE_BASIC 권한 필요]
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }
}
