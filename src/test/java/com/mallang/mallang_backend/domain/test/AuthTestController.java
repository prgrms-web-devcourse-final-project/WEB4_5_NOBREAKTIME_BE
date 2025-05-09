package com.mallang.mallang_backend.domain.test;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthTestController {

    @PreAuthorize("hasAnyRole('STANDARD', 'PREMIUM')")
    @GetMapping("/api/auth/test")
    public ResponseEntity<?> authTest() {
        return ResponseEntity.ok("권한 검증 확인");
    }
}
