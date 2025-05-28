package com.mallang.mallang_backend.domain.video.learning.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoLearningExpressionQuizListResponse {
	private List<VideoLearningExpressionQuizItem> quiz;
}