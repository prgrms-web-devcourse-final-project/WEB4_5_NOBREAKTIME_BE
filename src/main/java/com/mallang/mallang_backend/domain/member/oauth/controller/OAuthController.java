package com.mallang.mallang_backend.domain.member.oauth.controller;

import com.mallang.mallang_backend.domain.member.oauth.dto.RejoinBlockResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class OAuthController {

    @GetMapping("/rejoin-block")
    public ResponseEntity<RejoinBlockResponse> getRejoinBlockInfo() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new RejoinBlockResponse(409,
                "회원 탈퇴 이력이 30일 이내에 있어서 재가입이 불가능합니다."
        ));
    }
}
