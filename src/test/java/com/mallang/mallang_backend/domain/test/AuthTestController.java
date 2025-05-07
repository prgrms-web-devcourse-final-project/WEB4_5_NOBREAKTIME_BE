package com.mallang.mallang_backend.domain.test;

import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthTestController {

    @PreAuthorize("hasAnyRole('STANDARD', 'PREMIUM')")
    @GetMapping("/api/auth/test")
    public ResponseEntity<?> authTest() {
        return ResponseEntity.ok("권한 검증 확인");
    }
}
