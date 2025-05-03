package com.mallang.mallang_backend.domain.video.learning.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoLearningExpressionQuizListResponse {
	private final List<VideoLearningExpressionQuizItem> quiz;
}