package com.mallang.mallang_backend.domain.member.entity;

public enum LoginPlatform {
    LOCAL, GOOGLE, KAKAO, NAVER;

    public static LoginPlatform from(String provider) {
        provider = provider.toLowerCase();
        switch (provider) {
            case "kakao" : return LoginPlatform.KAKAO;
        }
        return LoginPlatform.LOCAL;
    }
}