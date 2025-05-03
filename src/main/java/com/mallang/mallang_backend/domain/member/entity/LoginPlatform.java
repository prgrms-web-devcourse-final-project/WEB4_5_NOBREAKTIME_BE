package com.mallang.mallang_backend.domain.member.entity;

public enum LoginPlatform {
    NONE, LOCAL, GOOGLE, KAKAO, NAVER;

    public static LoginPlatform from(String provider) {
        provider = provider.toLowerCase();
        switch (provider) {
            case "kakao" : return LoginPlatform.KAKAO;
            case "google" : return LoginPlatform.GOOGLE;
            case "naver" : return LoginPlatform.NAVER;
        }
        return LoginPlatform.LOCAL;
    }
}