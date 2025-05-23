package com.mallang.mallang_backend.global.exception.custom;

/**
 * 재시도 허용 예외
 */
public class RetryableException extends RuntimeException{
    public RetryableException(String message) {
        super(message);
    }

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
