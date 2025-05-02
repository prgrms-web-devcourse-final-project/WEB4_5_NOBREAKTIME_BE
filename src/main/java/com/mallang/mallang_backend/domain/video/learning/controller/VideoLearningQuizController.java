package com.mallang.mallang_backend.domain.video.learning.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizListResponse;
import com.mallang.mallang_backend.domain.video.learning.service.VideoLearningQuizService;
import com.mallang.mallang_backend.global.dto.RsData;

import lombok.RequiredArgsConstructor;

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
