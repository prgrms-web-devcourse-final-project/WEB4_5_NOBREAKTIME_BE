package com.mallang.mallang_backend.domain.video.learning.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningExpressionQuizListResponse;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizListResponse;
import com.mallang.mallang_backend.domain.video.learning.service.VideoLearningQuizService;
import com.mallang.mallang_backend.global.dto.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/videos/{videoId}/quiz")
@RequiredArgsConstructor
@Tag(name = "VideoLearningQuiz", description = "영상 학습용 퀴즈 조회 API")
public class VideoLearningQuizController {

	private final VideoLearningQuizService videoLearningQuizService;

	/**
	 * 영상 단어 퀴즈 조회
	 * @param videoId
	 * @return ResponseEntity<RsData<VideoLearningQuizListResponse>>
	 */
	@GetMapping("/words")
	@Operation(
		summary = "영상 단어 퀴즈 조회",
		description = "주어진 videoId에 대한 영상 단어 퀴즈 목록을 반환합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "단어 퀴즈 조회 성공")
		}
	)
	public ResponseEntity<RsData<VideoLearningWordQuizListResponse>> getWordsQuiz(
		@Parameter(description = "조회할 비디오 ID", example = "vid-001")
		@PathVariable String videoId
	) {
		VideoLearningWordQuizListResponse body = videoLearningQuizService.makeQuizList(videoId);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"영상 단어 퀴즈 조회 성공",
			body
		));
	}

	/**
	 * 영상 표현 퀴즈 조회
	 * @param videoId
	 * @return ResponseEntity<RsData<VideoLearningExpressionQuizListResponse>>
	 */
	@GetMapping("/expressions")
	@Operation(
		summary = "영상 표현 퀴즈 조회",
		description = "주어진 videoId에 대한 영상 표현 퀴즈 목록을 반환합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "표현 퀴즈 조회 성공")
		}
	)
	public ResponseEntity<RsData<VideoLearningExpressionQuizListResponse>> getExpressionQuiz(
		@Parameter(description = "조회할 비디오 ID", example = "vid-001")
		@PathVariable String videoId
	) {
		var body = videoLearningQuizService.makeExpressionQuizList(videoId);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"표현 퀴즈 문제 조회 성공",
			body
		));
	}
}
