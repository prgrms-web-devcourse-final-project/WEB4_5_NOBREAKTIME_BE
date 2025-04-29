package com.mallang.mallang_backend.global.common;

import lombok.Getter;

@Getter
public enum Language {
    ENGLISH("en-US"),
    JAPANESE("ja"),
    CHINESE("zh-cn"),
    NONE("none");

    private final String languageCode;

    Language(String languageCode) {
        this.languageCode = languageCode;
    }
}
