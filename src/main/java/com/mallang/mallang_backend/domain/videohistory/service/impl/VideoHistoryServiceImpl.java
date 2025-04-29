package com.mallang.mallang_backend.domain.videohistory.service.impl;

import com.mallang.mallang_backend.domain.videohistory.dto.VideoHistoryResponse;
import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import com.mallang.mallang_backend.domain.videohistory.mapper.VideoHistoryMapper;
import com.mallang.mallang_backend.domain.videohistory.repository.VideoHistoryRepository;
import com.mallang.mallang_backend.domain.videohistory.service.VideoHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class VideoHistoryServiceImpl implements VideoHistoryService {
    private final VideoHistoryRepository videoHistoryRepository;
    private final VideoHistoryMapper videoHistoryMapper;

    @Override
    @Transactional
    public void save(Long memberId, String videoId) {
        VideoHistory history = VideoHistory.builder()
            .memberId(memberId)
            .videoId(videoId)
            .build();
        videoHistoryRepository.save(history);
    }

    @Override
    public List<VideoHistoryResponse> getRecentHistories(Long memberId) {
        return videoHistoryRepository.findTop5ByIdMemberIdOrderByCreatedAtDesc(memberId)
            .stream()
            .map(videoHistoryMapper::toDto)
            .toList();
    }

    @Override
    public List<VideoHistoryResponse> getAllHistories(Long memberId) {
        return videoHistoryRepository.findAllByIdMemberIdOrderByCreatedAtDesc(memberId)
            .stream()
            .map(videoHistoryMapper::toDto)
            .toList();
    }
}
