package com.mallang.mallang_backend.domain.video.service;

import com.mallang.mallang_backend.domain.video.dto.VideoResponse;

import java.util.List;

public interface VideoService {
    List<VideoResponse> getVideosByLanguage(String language, long maxResults);
}
