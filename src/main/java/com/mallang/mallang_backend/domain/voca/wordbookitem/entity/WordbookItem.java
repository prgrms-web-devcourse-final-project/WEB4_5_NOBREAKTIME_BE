package com.mallang.mallang_backend.domain.voca.wordbookitem.entity;

import com.mallang.mallang_backend.domain.video.entity.Video;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordbookItem {

    @EmbeddedId
    private WordbookItemId id; // 단어 + 단어장 id 를 복합 키로 사용 -> 하나의 단어장 안에 포함되어있는 여러 단어들

    @Column(nullable = true, name = "video_id")
    private Long videoId;
    
    @Column(nullable = false)
    private String description; // 해석

    @Column(nullable = false)
    private String originalSentence; // 영상에서 발췌한 예문

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WordStatus wordStatus = WordStatus.NEW; // 기본

    @Column(nullable = false)
    private LocalDateTime lastStudiedAt = createdAt; // 기본 값, 이후에 추가로 공부하면 변경

    @Builder
    public WordbookItem(
        Long wordbookId,
        String word,
        String description,
        String originalSentence,
        Long videoId
    ) {
        this.id = new WordbookItemId(wordbookId, word);
        this.description = description;
        this.originalSentence = originalSentence;
        this.createdAt = LocalDateTime.now();
        this.wordStatus = WordStatus.NEW;
        this.videoId = videoId;
    }

    // 비즈니스 메서드
    public void updateStatus(WordStatus status) {
        this.wordStatus = status;
        this.lastStudiedAt = LocalDateTime.now();
    }
}