package com.mallang.mallang_backend.domain.video.learning.controller;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningExpressionQuizListResponse;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizListResponse;
import com.mallang.mallang_backend.domain.video.learning.service.VideoLearningQuizService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "VideoLearningQuiz", description = "영상 학습 퀴즈 관련 API")
@RestController
@RequestMapping("/api/v1/videos/{videoId}/quiz")
@RequiredArgsConstructor
public class VideoLearningQuizController {

	private final VideoLearningQuizService videoLearningQuizService;

	/**
	 * 영상 단어 퀴즈 조회
	 * @param videoId
	 * @return ResponseEntity<RsData<VideoLearningQuizListResponse>>
	 */
	@Operation(summary = "영상 단어 퀴즈 조회", description = "주어진 영상 ID로 단어 퀴즈 리스트를 조회합니다.")
	@ApiResponse(responseCode = "200", description = "영상 단어 퀴즈 조회 성공")
	@PossibleErrors({KEYWORD_NOT_FOUND})
	@GetMapping("/words")
	public ResponseEntity<RsData<VideoLearningWordQuizListResponse>> getWordsQuiz(
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
	@Operation(summary = "영상 표현 퀴즈 조회", description = "주어진 영상 ID로 표현 퀴즈 리스트를 조회합니다.")
	@ApiResponse(responseCode = "200", description = "영상 표현 퀴즈 조회 성공")
	@PossibleErrors({EXPRESSION_NOT_FOUND})
	@GetMapping("/expressions")
	public ResponseEntity<RsData<VideoLearningExpressionQuizListResponse>> getExpressionQuiz(
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