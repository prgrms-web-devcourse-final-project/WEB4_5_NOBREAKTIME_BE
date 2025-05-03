package com.mallang.mallang_backend.domain.video.video.controller;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import java.io.IOException;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.video.video.dto.AnalyzeVideoResponse;
import com.mallang.mallang_backend.domain.video.video.dto.VideoDetailResponse;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.video.service.VideoService;
import com.mallang.mallang_backend.domain.videohistory.event.VideoViewedEvent;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.filter.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.Login;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Video", description = "영상 분석 및 조회 관련 API")
@RestController
@RequestMapping("/api/v1/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final ApplicationEventPublisher publisher;

    /**
     * Youtube ID 로 영상을 분석해 원어 자막, 번역 자막, 핵심 단어를 응답하는 메서드
     *
     * @param youtubeVideoId 유튜브 영상의 ID, ex) DF3KVSnyUWI
     * @return 원어 자막, 번역 자막, 핵심 단어 리스트
     */
    @Operation(summary = "영상 분석", description = "Youtube ID로 영상을 분석하여 자막과 핵심 단어를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "영상 분석이 완료되었습니다.")
    @GetMapping("/{youtubeVideoId}/analysis")
    public ResponseEntity<RsData<AnalyzeVideoResponse>> videoAnalysis(
        @PathVariable String youtubeVideoId
    ) throws IOException, InterruptedException {
        AnalyzeVideoResponse response = videoService.analyzeVideo(youtubeVideoId);
        return ResponseEntity.ok(new RsData<>(
            "200",
            "영상 분석이 완료되었습니다.",
            response
        ));
    }

    /**
     * Clova Speech 에 음성 리소스를 제공하기 위한 메서드
     *
     * @param fileName 리소스 파일명
     * @return 음성 리소스
     */
    @Operation(summary = "오디오 파일 제공", description = "Clova Speech용 음성 리소스를 제공합니다.")
    @ApiResponse(responseCode = "200", description = "오디오 파일 제공 성공")
    @GetMapping("/uploaded/{fileName}")
    public ResponseEntity<RsData<byte[]>> getAudioFile(
        @PathVariable String fileName
    ) {
        try {
            byte[] audioData = videoService.getAudioFile(fileName);
            return ResponseEntity.ok(new RsData<>(
                "200",
                "오디오 파일 제공",
                audioData
            ));
        } catch (IOException e) {
            throw new ServiceException(AUDIO_FILE_NOT_FOUND);
        }
    }

    /**
     * Youtube API 를 통해 영상 목록을 가져오는 메서드(다건)
     *
     * @param q 검색어 쿼리
     * @param category 동영상 카테고리
     * @param language 동영상 언어
     * @param maxResults 결과값 갯수
     * @return 검색 결과 리스트
     */
    @Operation(summary = "영상 목록 조회", description = "조건에 맞는 영상 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "영상 목록 조회 완료")
    @GetMapping("/list")
    public ResponseEntity<RsData<List<VideoResponse>>> getVideoList(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "en") String language,
        @RequestParam(defaultValue = "10") long maxResults
    ) {
        List<VideoResponse> list = videoService.getVideosByLanguage(q, category, language, maxResults);
        return ResponseEntity.ok(new RsData<>(
            "200",
            "영상 목록 조회 완료",
            list
        ));
    }

    /**
     * 1) 비디오 조회 & upsert (동기)
     * 2) 히스토리 저장 이벤트 발행 (비동기)
     */
    @Operation(summary = "영상 상세 조회", description = "특정 영상의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "영상 상세정보 조회 완료")
    @PostMapping("/{videoId}")
    public ResponseEntity<RsData<VideoDetailResponse>> getVideo(
        @PathVariable String videoId,
        @Login CustomUserDetails userDetail
    ) {
        Long memberId = userDetail.getMemberId();

        // 1) 동기 처리: 조회 + 엔티티 저장/업데이트
        VideoDetailResponse dto = videoService.getVideoDetail(videoId);

        // 2) 비동기로 히스토리 저장 트리거
        publisher.publishEvent(new VideoViewedEvent(memberId, videoId));

        return ResponseEntity.ok(new RsData<>(
            "200",
            "영상 상세정보 조회 완료",
            dto
        ));
    }
}
