package com.mallang.mallang_backend.global.filter;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

import static com.mallang.mallang_backend.global.exception.ErrorCode.USER_NOT_FOUND;

@Getter
@Setter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private Long memberId;
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
