package com.mallang.mallang_backend.domain.video.video.dto;

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
}