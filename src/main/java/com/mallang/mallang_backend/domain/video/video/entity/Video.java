package com.mallang.mallang_backend.domain.video.video.entity;

import com.mallang.mallang_backend.global.common.Language;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "video_id")
    private String id;

    @Column(nullable = false)
    private String videoTitle;

    @Column(nullable = false)
    private String thumbnailImageUrl;

    @Column(nullable = false)
    private String channelTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Builder
    public Video(
        String id,
        String videoTitle,
        String thumbnailImageUrl,
        String channelTitle,
        Language language
    ) {
        this.id = id;
        this.videoTitle = videoTitle;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.channelTitle = channelTitle;
        this.language = language;
    }
}