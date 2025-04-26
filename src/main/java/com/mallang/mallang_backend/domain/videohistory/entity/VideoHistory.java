package com.mallang.mallang_backend.domain.videohistory.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VideoHistory {

    @EmbeddedId
    private VideoHistoryId id;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public VideoHistory(Long memberId, Long videoId) {
        id = new VideoHistoryId(memberId, videoId);
    }
}