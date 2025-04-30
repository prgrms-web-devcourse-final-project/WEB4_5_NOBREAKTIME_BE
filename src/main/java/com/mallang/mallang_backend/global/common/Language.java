package com.mallang.mallang_backend.global.common;

import java.util.Arrays;

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

    /**
     * API가 주는 code(ex: "en", "en-US", "ja", "zh", "zh-CN")를
     * 이 enum에 매핑하여 반환, 없을 경우 NONE 리턴
     */
    public static Language fromCode(String code) {
        if (code == null || code.isBlank()) {
            return NONE;
        }
        String normalized = code.toLowerCase();
        return Arrays.stream(values())
            .filter(lang -> {
                String lc = lang.languageCode.toLowerCase();
                // 정확히 일치하거나, 부분만 일치할 때
                return normalized.equals(lc)
                    || normalized.startsWith(lc.split("-")[0]);
            })
            .findFirst()
            .orElse(NONE);
    }
}
