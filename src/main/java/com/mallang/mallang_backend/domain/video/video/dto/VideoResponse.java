package com.mallang.mallang_backend.domain.video.video.dto;

import com.mallang.mallang_backend.domain.video.util.VideoUtils;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponse {
    private String videoId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private boolean isBookmarked;
    private String duration;

    public static VideoResponse from(Videos video, boolean isBookmarked, String isoDuration) {
        String formattedDuration = VideoUtils.formatDuration(isoDuration);
        return new VideoResponse(
                video.getId(),
                video.getVideoTitle(),
                "", // 필요 시 채널 설명
                video.getThumbnailImageUrl(),
                isBookmarked,
                formattedDuration
        );
    }
}