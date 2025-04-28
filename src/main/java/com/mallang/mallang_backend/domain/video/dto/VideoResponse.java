package com.mallang.mallang_backend.domain.video.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VideoResponse {
    // 프론트에 넘겨줄 유튜브 영상 정보 DTO
    private String videoId;
    private String title;
    private String description;
    private String thumbnailUrl;
}
