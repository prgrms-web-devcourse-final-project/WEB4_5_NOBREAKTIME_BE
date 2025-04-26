package com.mallang.mallang_backend.global.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String messageCode; // 프로퍼티 메시지

    public ServiceException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.messageCode = errorCode.getMessageCode();
    }

    public ServiceException(ErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
        this.messageCode = errorCode.getMessageCode();
    }
}
