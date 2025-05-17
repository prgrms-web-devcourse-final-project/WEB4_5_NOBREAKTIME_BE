package com.mallang.mallang_backend.domain.video.video.controller;

import com.mallang.mallang_backend.domain.video.video.dto.VideoListRequest;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.video.service.VideoService;
import com.mallang.mallang_backend.domain.video.youtube.YoutubeCategoryId;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.login.Login;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Tag(name = "Video", description = "영상 분석 및 조회 관련 API")
@Validated
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
    @GetMapping(
        value = "/{youtubeVideoId}/analysis",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter videoAnalysis(
        @PathVariable String youtubeVideoId,
        @Parameter(hidden = true) @Login CustomUserDetails userDetail
    ) {
        Long memberId = userDetail.getMemberId();
        // 0L을 주면 타임아웃 없이 무제한 대기
        SseEmitter emitter = new SseEmitter(0L);

        // 초기 연결 알림
        try {
            emitter.send(SseEmitter.event().name("INIT").data("스트림 연결 성공"));
        } catch (IOException e) {
            emitter.completeWithError(e);
            return emitter;
        }

        videoService.analyzeWithSseAsync(memberId, youtubeVideoId, emitter);
        return emitter;
    }

    /**
     * Youtube API 를 통해 영상 목록을 가져오는 메서드(다건)
     * 회원의 언어 설정에 맞춰 필터링된 영상 목록을 조회합니다.
     * @return 검색 결과 리스트
     */
    @Operation(summary = "영상 목록 조회", description = "조건에 맞는 영상 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "영상 목록 조회 완료")
    @PossibleErrors({MEMBER_NOT_FOUND, LANGUAGE_NOT_CONFIGURED, VIDEO_ID_SEARCH_FAILED, VIDEO_DETAIL_FETCH_FAILED, API_ERROR, CATEGORY_NOT_FOUND})
    @GetMapping("/list")
    public ResponseEntity<RsData<List<VideoResponse>>> getVideoList(
        @Valid @ModelAttribute VideoListRequest req,
        @Parameter(hidden = true)
        @Login CustomUserDetails userDetail
    ) {
        // DTO 에서 String으로 받은 category
        String rawCategory = req.getCategory();

        String categoryId = null;
        if (rawCategory != null && !rawCategory.isBlank()) {
            categoryId = YoutubeCategoryId.of(rawCategory).getId();
        }

        List<VideoResponse> list = videoService.getVideosForMember(
            req.getQ(),
            categoryId,
            req.getMaxResults(),
            userDetail.getMemberId()
        );

        return ResponseEntity.ok(new RsData<>(
            "200",
            "영상 목록 조회 완료",
            list
        ));
    }
}
