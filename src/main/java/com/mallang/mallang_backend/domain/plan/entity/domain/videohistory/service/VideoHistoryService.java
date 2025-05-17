package com.mallang.mallang_backend.domain.plan.entity.domain.videohistory.service;

import com.mallang.mallang_backend.domain.plan.entity.domain.videohistory.dto.VideoHistoryResponse;

import java.util.List;

public interface VideoHistoryService {
    void save(Long memberId, String videoId);

    List<VideoHistoryResponse> getRecentHistories(Long memberId); // 최근 5개

    List<VideoHistoryResponse> getAllHistories(Long memberId); // 전체
}

