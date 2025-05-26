package com.mallang.mallang_backend.global.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * [주의] ALL은 실제 언어가 아닌 권한 설정용 특수 값입니다.
 * 프리미엄 회원에게만 할당될 수 있습니다.
 */
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
     * ALL일 경우 ENGLISH, JAPANESE 모두 반환
     * 그 외에는 자기 자신만 반환
     */
    public List<Language> getAvailableLanguages() {
        if (this == ALL) {
            return List.of(ENGLISH, JAPANESE);
        }
        if (this == NONE) {
            return List.of();
        }
        return List.of(this);
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

    /**
     * ALL일 경우 ENGLISH, JAPANESE 둘 다 매칭 허용
     */
    public boolean matches(String word) {
        if (this == NONE) {
            return false;
        }
        if (this == ALL) {
            // ENGLISH 또는 JAPANESE 중 하나라도 매칭되면 true
            return ENGLISH.matches(word) || JAPANESE.matches(word);
        }
        Pattern compile = Pattern.compile(pattern);
        return compile.matcher(word).matches();
    }
}
