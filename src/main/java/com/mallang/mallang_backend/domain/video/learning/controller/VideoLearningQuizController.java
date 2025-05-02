package com.mallang.mallang_backend.domain.video.learning.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizListResponse;
import com.mallang.mallang_backend.domain.video.learning.service.VideoLearningQuizService;
import com.mallang.mallang_backend.global.dto.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

@Tag(name = "VideoLearningQuiz", description = "영상 학습 단어 퀴즈 관련 API")
@RestController
@RequestMapping("/api/v1/videos/{videoId}/quiz")
@RequiredArgsConstructor
public class VideoLearningQuizController {

	private final VideoLearningQuizService videoLearningQuizService;

	/**
	 * 영상 단어 퀴즈 조회
	 *
	 * @param videoId 영상 ID
	 * @return 영상에 대한 단어 퀴즈 리스트
	 */
	@Operation(summary = "영상 단어 퀴즈 조회", description = "주어진 영상 ID로 단어 퀴즈 리스트를 조회합니다.")
	@ApiResponse(responseCode = "200", description = "영상 단어 퀴즈 조회 성공")
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
}
