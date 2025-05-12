package com.mallang.mallang_backend.domain.video.video.controller;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

        // 별도 스레드에서 분석 로직 + SSE 전송
        CompletableFuture.runAsync(() -> {
            try {
                // 단계별 진행 알림을 보내고, 마지막에 AnalyzeVideoResponse 객체를 리턴
                AnalyzeVideoResponse result =
                    videoService.analyzeVideo(memberId, youtubeVideoId, emitter);

                // (선택) 분석 완료 시 최종 페이로드 전송
                emitter.send(SseEmitter.event()
                    .name("analysisComplete")
                    .data(result)
                );

                emitter.complete();
            } catch (IOException | InterruptedException ex) {
                emitter.completeWithError(ex);
            }
        });

        return emitter;
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
