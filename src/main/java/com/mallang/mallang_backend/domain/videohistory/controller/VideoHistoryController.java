package com.mallang.mallang_backend.domain.videohistory.controller;

import com.mallang.mallang_backend.domain.videohistory.dto.VideoHistoryResponse;
import com.mallang.mallang_backend.domain.videohistory.service.VideoHistoryService;
import com.mallang.mallang_backend.global.dto.RsData;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/videohistory")
public class VideoHistoryController {
    private final VideoHistoryService videoHistoryService;

    // 시청 기록 저장
    @PostMapping("/{videoId}")
    public ResponseEntity<RsData<Void>> save(@PathVariable String videoId, @RequestParam Long memberId) {
        videoHistoryService.save(memberId, videoId);
        return ResponseEntity.ok(new RsData<>(
            "200",
            "시청 기록 저장 완료 (memberId: " + memberId + ", videoId: " + videoId + ")"
        ));
    }

    // 최근 시청한 영상 기록 조회 (5개)
    @GetMapping("/videos/summary")
    public ResponseEntity<RsData<List<VideoHistoryResponse>>> getRecentVideos(@RequestParam Long memberId) {
        List<VideoHistoryResponse> recentHistories = videoHistoryService.getRecentHistories(memberId);
        return ResponseEntity.ok(new RsData<>(
            "200",
            "최근 시청 기록 조회 완료",
            recentHistories
        ));
    }

    // 전체 시청 영상
    @GetMapping("/videos/history")
    public ResponseEntity<RsData<List<VideoHistoryResponse>>> getFullHistory(@RequestParam Long memberId) {
        List<VideoHistoryResponse> allHistories = videoHistoryService.getAllHistories(memberId);
        return ResponseEntity.ok(new RsData<>(
            "200",
            "전체 시청 영상 조회 완료",
            allHistories
        ));
    }
}
