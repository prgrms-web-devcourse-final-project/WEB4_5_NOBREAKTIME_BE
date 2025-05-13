package com.mallang.mallang_backend.domain.videohistory.service;

import java.util.List;

import com.mallang.mallang_backend.domain.videohistory.dto.VideoHistoryResponse;

public interface VideoHistoryService {
    void save(Long memberId, String videoId);

    List<VideoHistoryResponse> getRecentHistories(Long memberId); // 최근 5개

    List<VideoHistoryResponse> getHistoriesByPage(Long memberId, int page, int size);
}

