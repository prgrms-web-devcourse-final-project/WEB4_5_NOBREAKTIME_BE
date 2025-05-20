package com.mallang.mallang_backend.global.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.regex.Pattern;

@Getter
public enum Language {
    ENGLISH("en-US", "^[a-zA-Z\\s]+$"),
    JAPANESE("ja", "^[\\u3040-\\u309F\\u30A0-\\u30FF\\uFF66-\\uFF9F\\u4E00-\\u9FFF\\s]+$"),
    NONE("none", ""),
    ALL("all", ""); // 프리미엄 회원의 경우

    private final String languageCode;
    private final String pattern;

    Language(String languageCode, String pattern) {
        this.languageCode = languageCode;
        this.pattern = pattern;
    }

    /**
     * API가 주는 code(ex: "en", "en-US", "ja")를
     * 이 enum에 매핑하여 반환, 없을 경우 NONE 리턴
     */
    public static Language fromCode(String code) {
        if (code == null || code.isBlank()) {
            return NONE;
        }

        if (code.equals("all")) {
            return ALL; // 임시 변경
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

    /**
     * 이 enum의 언어코드를 ISO 코드로 변환
     */
    public String toCode() {
        if (this == NONE) {
            return "";
        }
        String[] parts = this.languageCode.split("-");
        return parts[0].toLowerCase();
    }

    public boolean matches(String word) {
        if (this == NONE || this == ALL) {
            return false;
        }
        Pattern compile = Pattern.compile(pattern);
        return compile.matcher(word).matches();
    }
}
