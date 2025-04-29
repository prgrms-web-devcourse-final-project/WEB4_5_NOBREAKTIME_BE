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
    TOKEN_EXPIRED("401-1", "token.expired", HttpStatus.UNAUTHORIZED),

    VIDEO_ID_SEARCH_FAILED("500-1", "video.id.search.failed", HttpStatus.INTERNAL_SERVER_ERROR),

    VIDEO_DETAIL_FETCH_FAILED("500-2", "video.detail.fetch.failed", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String messageCode; // 메시지 프로퍼티
    private final HttpStatus status;
}
