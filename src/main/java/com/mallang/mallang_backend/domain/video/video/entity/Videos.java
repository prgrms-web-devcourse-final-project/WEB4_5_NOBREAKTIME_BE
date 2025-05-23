package com.mallang.mallang_backend.domain.video.video.entity;

import java.util.ArrayList;
import java.util.List;

import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.entity.BaseTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Videos extends BaseTime {

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

    @Column(nullable = false)
    private String duration;

    @OneToMany(mappedBy = "videos", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Subtitle> subtitles = new ArrayList<>();

    @Builder
    private Videos(
        String id,
        String videoTitle,
        String thumbnailImageUrl,
        String channelTitle,
        Language language,
        String duration
    ) {
        this.id = id;
        this.videoTitle = videoTitle;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.channelTitle = channelTitle;
        this.language = language;
        this.duration = duration;
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