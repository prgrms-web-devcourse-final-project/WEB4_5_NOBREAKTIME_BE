package com.mallang.mallang_backend.global.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LanguageTest {

    @Test
    @DisplayName("fromCode: 'en' 또는 'en-US' → ENGLISH 매핑")
    void testEnglishMapping() {
        assertThat(Language.fromCode("en")).isEqualTo(Language.ENGLISH);
        assertThat(Language.fromCode("en-US")).isEqualTo(Language.ENGLISH);
        assertThat(Language.fromCode("EN-us")).isEqualTo(Language.ENGLISH);
    }

    @Test
    @DisplayName("fromCode: 'ja' → JAPANESE 매핑")
    void testJapaneseMapping() {
        assertThat(Language.fromCode("ja")).isEqualTo(Language.JAPANESE);
    }

    @Test
    @DisplayName("fromCode: null, 빈 문자열, 알 수 없는 코드 → NONE 매핑")
    void testNoneMapping() {
        assertThat(Language.fromCode(null)).isEqualTo(Language.NONE);
        assertThat(Language.fromCode("")).isEqualTo(Language.NONE);
        assertThat(Language.fromCode("ko")).isEqualTo(Language.NONE);
        assertThat(Language.fromCode("fr")).isEqualTo(Language.NONE);
    }
}
