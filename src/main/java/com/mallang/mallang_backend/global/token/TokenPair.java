package com.mallang.mallang_backend.global.token;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenPair {

    private final String accessToken;
    private final String refreshToken;
}
