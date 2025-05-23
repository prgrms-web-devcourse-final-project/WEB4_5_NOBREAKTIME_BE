package com.mallang.mallang_backend.domain.member.oauth.service;

import java.security.SecureRandom;

import static com.mallang.mallang_backend.global.constants.AppConstants.CHARACTERS;

public class RandomStringGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}