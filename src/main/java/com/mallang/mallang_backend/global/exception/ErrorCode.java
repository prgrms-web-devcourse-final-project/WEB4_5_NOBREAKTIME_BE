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

    // GPT Errors
    GPT_RESPONSE_PARSE_FAIL("500-1", "gpt.response.parse.fail", HttpStatus.INTERNAL_SERVER_ERROR),
    GPT_API_CALL_FAILED("500-4", "gpt.api.call.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    GPT_RESPONSE_EMPTY("500-5", "gpt.response.empty", HttpStatus.INTERNAL_SERVER_ERROR),


    // Word Errors (추가)
    WORD_SAVE_FAILED("500-2", "word.save.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    WORD_PARSE_FAILED("500-3", "word.parse.failed", HttpStatus.INTERNAL_SERVER_ERROR);


    private final String code;
    private final String messageCode; // 메시지 프로퍼티
    private final HttpStatus status;
}
