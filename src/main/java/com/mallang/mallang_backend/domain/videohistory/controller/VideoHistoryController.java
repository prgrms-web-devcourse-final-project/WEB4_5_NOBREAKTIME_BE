package com.mallang.mallang_backend.domain.videohistory.controller;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.videohistory.dto.VideoHistoryResponse;
import com.mallang.mallang_backend.domain.videohistory.service.VideoHistoryService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.Login;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "VideoHistory", description = "시청 기록 관련 API")
@RestController
@RequestMapping("/api/v1/videohistory")
@RequiredArgsConstructor
public class VideoHistoryController {
    private final VideoHistoryService videoHistoryService;

    /**
     * 최근 시청한 영상 기록 조회 (5개)
     *
     * @param userDetail 로그인한 사용자의 정보
     * @return 최근 5개 시청 기록
     */
    @Operation(summary = "최근 시청 기록 조회", description = "최근에 시청한 영상 5개의 기록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "최근 시청 기록 조회 완료")
    @PossibleErrors({MEMBER_NOT_FOUND, API_ERROR})
    @GetMapping("/videos/summary")
    public ResponseEntity<RsData<List<VideoHistoryResponse>>> getRecentVideos(
        @Login CustomUserDetails userDetail
    ) {
        Long memberId = userDetail.getMemberId();
        List<VideoHistoryResponse> recentHistories = videoHistoryService.getRecentHistories(memberId);
        return ResponseEntity.ok(new RsData<>(
            "200",
            "최근 시청 기록 조회 완료",
            recentHistories
        ));
    }

    /**
     * 전체 시청 영상 조회
     *
     * @param userDetail 로그인한 사용자의 정보
     * @return 전체 시청 기록
     */
    @Operation(summary = "전체 시청 기록 조회", description = "사용자가 지금까지 시청한 모든 영상 기록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "전체 시청 영상 조회 완료")
    @PossibleErrors({MEMBER_NOT_FOUND, API_ERROR})
    @GetMapping("/videos/history")
    public ResponseEntity<RsData<List<VideoHistoryResponse>>> getFullHistory(
        @Login CustomUserDetails userDetail
    ) {
        Long memberId = userDetail.getMemberId();
        List<VideoHistoryResponse> allHistories = videoHistoryService.getAllHistories(memberId);
        return ResponseEntity.ok(new RsData<>(
            "200",
            "전체 시청 영상 조회 완료",
            allHistories
        ));
    }
}
