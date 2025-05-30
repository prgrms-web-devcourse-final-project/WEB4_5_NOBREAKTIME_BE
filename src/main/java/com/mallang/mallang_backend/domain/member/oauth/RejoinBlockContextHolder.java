package com.mallang.mallang_backend.domain.member.oauth;

import com.mallang.mallang_backend.domain.member.oauth.dto.RejoinBlockResponse;

public class RejoinBlockContextHolder {
    private static final ThreadLocal<RejoinBlockResponse> context = new ThreadLocal<>();

    public static void set(RejoinBlockResponse response) {
        context.set(response);
    }

    public static RejoinBlockResponse get() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}