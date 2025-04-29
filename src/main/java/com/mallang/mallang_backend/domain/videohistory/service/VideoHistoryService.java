package com.mallang.mallang_backend.domain.videohistory.service;

import com.mallang.mallang_backend.domain.videohistory.dto.VideoHistoryResponse;

import java.util.List;

public interface VideoHistoryService {
    void save(Long memberId, Long videoId);

    List<VideoHistoryResponse> getRecentHistories(Long memberId); // 최근 5개

    List<VideoHistoryResponse> getAllHistories(Long memberId); // 전체
}

