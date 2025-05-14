package com.mallang.mallang_backend.domain.videohistory.controller;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.videohistory.dto.VideoHistoryResponse;
import com.mallang.mallang_backend.domain.videohistory.service.VideoHistoryService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.login.Login;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
        @Parameter(hidden = true)
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

    @Operation(
        summary = "페이징된 시청 기록 조회",
        description = "page, size 파라미터로 페이지 단위 시청 기록을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "페이징 시청 기록 조회 완료")
    @PossibleErrors({ MEMBER_NOT_FOUND, API_ERROR })
    @GetMapping("/videos/history")
    public ResponseEntity<RsData<List<VideoHistoryResponse>>> getHistoriesByPage(
        @Parameter(hidden = true)
        @Login CustomUserDetails userDetail,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = userDetail.getMemberId();
        // 마지막에 본 것부터 차례로 정렬
        List<VideoHistoryResponse> pageData =
            videoHistoryService.getHistoriesByPage(memberId, page, size);
        return ResponseEntity.ok(new RsData<>(
            "200",
            String.format("시청 기록 조회 완료 (page=%d, size=%d)", page, size),
            pageData
        ));
    }
}
