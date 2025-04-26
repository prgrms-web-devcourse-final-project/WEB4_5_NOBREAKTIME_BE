package com.mallang.mallang_backend.domain.voca.word;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Difficulty {
    EASY(1),
    NORMAL(2),
    HARD(3),
    VERY_HARD(4),
    EXTREME(5);

    private final int value;

    // 숫자로부터 Enum 을 역으로 찾고 싶을 때
    public static Difficulty fromValue(int value) {
        for (Difficulty d : Difficulty.values()) {
            if (d.value == value) {
                return d;
            }
        }
        throw new IllegalArgumentException("알 수 없는 값: " + value);
    }
}