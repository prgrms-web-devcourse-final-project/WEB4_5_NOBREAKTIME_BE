package com.mallang.mallang_backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // User Errors
    USER_NOT_FOUND("404-1", "user.not.found", HttpStatus.NOT_FOUND),

    // Token Errors
    TOKEN_EXPIRED("401-1", "token.expired", HttpStatus.UNAUTHORIZED);


    private final String code;
    private final String messageCode; // 메시지 프로퍼티
    private final HttpStatus status;
}
