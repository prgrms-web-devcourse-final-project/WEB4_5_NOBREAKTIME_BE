package com.mallang.mallang_backend.domain.video.learning.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoLearningWordQuizListResponse {
	private List<VideoLearningWordQuizItem> quiz;
}
