package com.mallang.mallang_backend.domain.videohistory.dto;

import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoHistoryResponse {
    private String videoId;
    private String title;
    private String thumbnailUrl;
    private LocalDateTime lastViewedAt;
    private String duration;

    public static VideoHistoryResponse from(VideoHistory history) {
        Videos video = history.getVideos();
        return new VideoHistoryResponse(
            video.getId(),
            video.getVideoTitle(),
            video.getThumbnailImageUrl(),
            history.getLastViewedAt(),
            video.getDuration()
        );
    }
}
