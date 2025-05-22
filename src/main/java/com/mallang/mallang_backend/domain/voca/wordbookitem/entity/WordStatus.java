package com.mallang.mallang_backend.domain.voca.wordbookitem.entity;

import java.time.Duration;

/**
 * 단어의 학습 상태
 */
public enum WordStatus {
    /**
     * 새로운 단어
     */
    NEW(0),
    /**
     * 틀린 단어 - 1일 후 복습하게 될 단어
     */
    WRONG(1),
    /**
     * 1회 복습한 단어 - 1주 후 복습하게 될 단어
     */
    REVIEW_COUNT_1(7),
    /**
     * 2회 복습한 단어 - 1개월 후 복습하게 될 단어
     */
    REVIEW_COUNT_2(30),
    /**
     * 3회 복습한 단어 - 3개월 후 복습하게 될 단어
     */
    REVIEW_COUNT_3(90),
    /**
     * 맞힌, 또는 4회 복습한 단어 - 6개월 후 복습하게 될 단어
     */
    CORRECT(180),
    /**
     * 아는 단어 - 맞힌 단어를 또 맞힌 경우
     */
    MASTERED(0);

    /**
     * 단어 상태에 따른 마지막 학습일 기준 복습 주기
     */
    private final int reviewInterval;

    WordStatus(int reviewInterval) {
        this.reviewInterval = reviewInterval;
    }

    /**
     * 단어 상태에 대한 복습 주기를 반환합니다.
     * @return 단어 상태에 대한 복습 주기 Duration
     */
    public Duration getReviewIntervalDuration() {
        if (this == NEW || this == MASTERED) {
            return Duration.ZERO;
        }
        return Duration.ofDays(reviewInterval);
    }
}
