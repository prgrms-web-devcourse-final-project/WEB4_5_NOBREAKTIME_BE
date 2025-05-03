package com.mallang.mallang_backend.domain.voca.wordbookitem.entity;

/**
 * 단어의 학습 상태
 */
public enum WordStatus {
    /** 새로운 단어 */
    NEW,
    /** 틀린 단어 - 1일 후 복습하게 될 단어 */
    WRONG,
    /** 1회 복습한 단어 - 1주 후 복습하게 될 단어 */
    REVIEW_COUNT_1,
    /** 2회 복습한 단어 - 1개월 후 복습하게 될 단어 */
    REVIEW_COUNT_2,
    /** 3회 복습한 단어 - 3개월 후 복습하게 될 단어 */
    REVIEW_COUNT_3,
    /** 맞힌, 또는 4회 복습한 단어 - 6개월 후 복습하게 될 단어 */
    CORRECT,
    /** 아는 단어 - 맞힌 단어를 또 맞힌 경우 */
    MASTERED
}
