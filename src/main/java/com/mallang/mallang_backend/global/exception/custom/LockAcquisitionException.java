package com.mallang.mallang_backend.global.exception.custom;

import com.mallang.mallang_backend.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class LockAcquisitionException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String messageCode;


    public LockAcquisitionException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.messageCode = errorCode.getMessageCode();
    }
}
