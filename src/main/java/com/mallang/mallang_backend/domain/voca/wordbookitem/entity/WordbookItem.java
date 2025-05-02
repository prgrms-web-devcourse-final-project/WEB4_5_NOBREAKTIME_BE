package com.mallang.mallang_backend.domain.voca.wordbookitem.entity;

import java.time.LocalDateTime;

import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordbookItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wordbook_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wordbook_id", nullable = false)
    private Wordbook wordbook;

    @Column(nullable = false)
    private String word;

    @Column(nullable = true, name = "video_id")
    private String videoId;

    @Column(nullable = true, name = "subtitle_id")
    private Long subtitleId; // 해석

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WordStatus wordStatus = WordStatus.NEW; // 기본

    @Column(nullable = false)
    private LocalDateTime lastStudiedAt = createdAt; // 기본 값, 이후에 추가로 공부하면 변경

    @Column(nullable = false)
    private boolean isLearned = false;

    @Builder
    public WordbookItem(
        Wordbook wordbook,
        String word,
        Long subtitleId,
        String videoId
    ) {
        this.wordbook = wordbook;
        this.word = word;
        this.subtitleId = subtitleId;
        this.videoId = videoId;
    }

    // 비즈니스 메서드
    public void updateStatus(WordStatus status) {
        this.wordStatus = status;
        this.lastStudiedAt = LocalDateTime.now();
    }

    public void updateLearned(boolean isLearned) {
        this.isLearned = isLearned;
    }

    public void updateLastStudiedAt(LocalDateTime lastStudiedAt) {
        this.lastStudiedAt = lastStudiedAt;
    }

    /**
     * 통합 단어 학습 결과에 따라 단어의 상태를 변경합니다.
     * @param isCorrect 정답 여부
     */
    public void applyLearningResult(Boolean isCorrect) {
        this.lastStudiedAt = LocalDateTime.now();

        if (!isCorrect) {
            this.wordStatus = WordStatus.WRONG;
            return;
        }

        switch (this.wordStatus) {
            case NEW -> this.wordStatus = WordStatus.CORRECT;
            case WRONG -> this.wordStatus = WordStatus.REVIEW_COUNT_1;
            case REVIEW_COUNT_1 -> this.wordStatus = WordStatus.REVIEW_COUNT_2;
            case REVIEW_COUNT_2 -> this.wordStatus = WordStatus.REVIEW_COUNT_3;
            case REVIEW_COUNT_3 -> this.wordStatus = WordStatus.CORRECT;
            case CORRECT -> this.wordStatus = WordStatus.MASTERED;
        }
    }
}