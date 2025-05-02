package com.mallang.mallang_backend.domain.video.learning.service;

import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizListResponse;

public interface VideoLearningQuizService {
	/**
	 * 주어진 videoId에 해당하는 빈칸 퀴즈 목록을 생성해서 반환
	 */
	VideoLearningWordQuizListResponse makeQuizList(String videoId);
}
