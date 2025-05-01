package com.mallang.mallang_backend.domain.sentence.expression.entity;


import java.time.LocalTime;

import com.mallang.mallang_backend.domain.video.video.entity.Videos;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expression {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expression_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Videos videos;

    @Column(nullable = false)
    private String sentence;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sentenceAnalysis;

    @Column(nullable = false)
    private LocalTime subtitleAt;

    @Builder
    public Expression(
        String sentence,
        String description,
        String sentenceAnalysis,
        Videos videos,
        LocalTime subtitleAt
    ) {
        this.sentence = sentence;
        this.description = description;
        this.sentenceAnalysis = sentenceAnalysis;
        this.videos = videos;
        this.subtitleAt = subtitleAt;
    }
}