package com.mallang.mallang_backend.domain.video.learning.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VideoLearningExpressionQuizListResponse {
	private final List<VideoLearningExpressionQuizItem> quiz;
}