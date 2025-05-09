package com.mallang.mallang_backend.domain.video.video.controller;

import com.mallang.mallang_backend.domain.video.video.dto.AnalyzeVideoResponse;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.video.service.VideoService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.login.Login;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Tag(name = "Video", description = "영상 분석 및 조회 관련 API")
@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /**
     * Youtube ID 로 영상을 분석해 원어 자막, 번역 자막, 핵심 단어를 응답하는 메서드
     *
     * @param youtubeVideoId 유튜브 영상의 ID, ex) DF3KVSnyUWI
     * @return 원어 자막, 번역 자막, 핵심 단어 리스트
     */
    @Operation(summary = "영상 분석", description = "Youtube ID로 영상을 분석하여 자막과 핵심 단어를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "영상 분석이 완료되었습니다.")
    @PossibleErrors({VIDEO_ID_SEARCH_FAILED, AUDIO_DOWNLOAD_FAILED, API_ERROR})
    @GetMapping("/{youtubeVideoId}/analysis")
    public ResponseEntity<RsData<AnalyzeVideoResponse>> videoAnalysis(
        @PathVariable String youtubeVideoId,
        @Parameter(hidden = true)
        @Login CustomUserDetails userDetail
    ) {
        Long memberId = userDetail.getMemberId();

        AnalyzeVideoResponse response;

        try {
            response = videoService.analyzeVideo(memberId, youtubeVideoId);
        } catch (IOException | InterruptedException e) {
            throw new ServiceException(AUDIO_DOWNLOAD_FAILED);
        }

        return ResponseEntity.ok(new RsData<>(
            "200",
            "영상 분석이 완료되었습니다.",
            response
        ));
    }

    /**
     * Youtube API 를 통해 영상 목록을 가져오는 메서드(다건)
     * 회원의 언어 설정에 맞춰 필터링된 영상 목록을 조회합니다.
     * @param q 검색어 쿼리
     * @param category 동영상 카테고리
     * @param maxResults 결과값 갯수
     * @return 검색 결과 리스트
     */
    @Operation(summary = "영상 목록 조회", description = "조건에 맞는 영상 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "영상 목록 조회 완료")
    @PossibleErrors({MEMBER_NOT_FOUND, LANGUAGE_NOT_CONFIGURED, VIDEO_ID_SEARCH_FAILED, VIDEO_DETAIL_FETCH_FAILED, API_ERROR})
    @GetMapping("/list")
    public ResponseEntity<RsData<List<VideoResponse>>> getVideoList(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "100") long maxResults,
        @Parameter(hidden = true)
        @Login CustomUserDetails userDetail
    ) {
        List<VideoResponse> list = videoService.getVideosForMember(
            q,
            category,
            maxResults,
            userDetail.getMemberId()
        );

        return ResponseEntity.ok(new RsData<>(
            "200",
            "영상 목록 조회 완료",
            list
        ));
    }
}
