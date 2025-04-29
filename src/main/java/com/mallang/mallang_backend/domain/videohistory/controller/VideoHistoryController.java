package com.mallang.mallang_backend.domain.videohistory.controller;

import com.mallang.mallang_backend.domain.videohistory.dto.VideoHistoryResponse;
import com.mallang.mallang_backend.domain.videohistory.service.VideoHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/videohistory")
public class VideoHistoryController {
    private final VideoHistoryService videoHistoryService;

    // 시청 기록 저장
    @PostMapping("/{videoId}")
    public void save(@PathVariable Long videoId,
                     @AuthenticationPrincipal(expression = "member.id") Long memberId) {
        videoHistoryService.save(memberId, videoId);
    }

    // 최근 시청한 영상 기록 조회 (5개)
    @GetMapping("/videos/summary")
    public List<VideoHistoryResponse> getRecentVideos(@AuthenticationPrincipal(expression = "member.id") Long memberId) {
        return videoHistoryService.getRecentHistories(memberId);
    }

    // 전체 시청 영상
    @GetMapping("/videos/history")
    public List<VideoHistoryResponse> getFullHistory(@AuthenticationPrincipal(expression = "member.id") Long memberId) {
        return videoHistoryService.getAllHistories(memberId);
    }
}
