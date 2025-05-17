package com.mallang.mallang_backend.domain.plan.entity.domain.keyword.entity;

import com.mallang.mallang_backend.domain.plan.entity.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.plan.entity.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.plan.entity.domain.voca.word.entity.Difficulty;
import com.mallang.mallang_backend.global.entity.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**

 핵심 단어*/
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Videos videos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtitle_id", nullable = false)
    private Subtitle subtitles;

    private String word; // 단어

    private String meaning; // 단어 뜻

    private Difficulty difficulty;  // 난이도

    @Builder
    public Keyword(Videos videos, Subtitle subtitle, String word, String meaning, Difficulty difficulty) {
        this.videos = videos;
        this.subtitles = subtitle;
        this.word = word;
        this.meaning = meaning;
        this.difficulty = difficulty;
    }
}