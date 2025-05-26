
package com.mallang.mallang_backend.global.exception.custom;

public class OAuthLoginException extends RuntimeException {

    public OAuthLoginException(String message) {
        super(message);
    }
}
