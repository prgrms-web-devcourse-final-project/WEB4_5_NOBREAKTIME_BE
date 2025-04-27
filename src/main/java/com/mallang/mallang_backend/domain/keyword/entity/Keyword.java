package com.mallang.mallang_backend.domain.keyword.entity;

import com.mallang.mallang_backend.domain.video.entity.Video;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * 핵심 단어
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    private String word; // 단어

    private LocalTime subtitleAt; // 자막의 시점 (대화 시작) -> 변경할 수 있음

    @Builder
    public Keyword(Video video, String word, LocalTime subtitleAt) {
        this.video = video;
        this.word = word;
        this.subtitleAt = subtitleAt;
    }
}
