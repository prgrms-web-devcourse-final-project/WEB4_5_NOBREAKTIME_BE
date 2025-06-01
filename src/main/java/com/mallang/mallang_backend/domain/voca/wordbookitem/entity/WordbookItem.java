package com.mallang.mallang_backend.domain.voca.wordbookitem.entity;

import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.global.entity.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "wordbook_item",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_wordbook_word",
        columnNames = {"wordbook_id", "word"}
    )
)
public class WordbookItem extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wordbook_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wordbook_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Wordbook wordbook;

    @Column(nullable = false)
    private String word;

    @Column(nullable = true, name = "video_id")
    private String videoId;

    @Column(nullable = true, name = "subtitle_id")
    private Long subtitleId; // 해석

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WordStatus wordStatus = WordStatus.NEW; // 기본

    @Column(nullable = false)
    private LocalDateTime lastStudiedAt = LocalDateTime.now(); // 기본 값, 이후에 추가로 공부하면 변경

    @Column(nullable = false)
    private boolean learned = false;

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
        this.learned = isLearned;
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

    public void updateWordbook(Wordbook toWordbook) {
        if (this.wordbook.equals(toWordbook)) {
            return;
        }

        this.wordbook = toWordbook;
    }
}