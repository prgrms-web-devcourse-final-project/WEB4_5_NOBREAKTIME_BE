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
public class Videos {

    @Id
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
    private Videos(
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

    /**
     * 필드 변경 시 사용하는 메서드
     */
    public void updateTitleAndThumbnail(
        String newTitle,
        String newThumbnailUrl,
        String newChannelTitle,
        Language newLanguage
    ) {
        this.videoTitle = newTitle;
        this.thumbnailImageUrl = newThumbnailUrl;
        this.channelTitle = newChannelTitle;
        this.language = newLanguage;
    }
}