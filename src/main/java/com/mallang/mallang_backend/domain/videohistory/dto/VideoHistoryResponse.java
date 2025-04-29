package com.mallang.mallang_backend.domain.videohistory.dto;

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
    private Long videoId;
    private String title;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
}

