package com.mallang.mallang_backend.domain.video.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VideoResponse {
    private String videoId;
    private String title;
    private String description;
    private String thumbnailUrl;
}
