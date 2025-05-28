package com.mallang.mallang_backend.domain.video.video.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoListRequest {

	@Schema(description = "검색어 (1~100자)", required = false)
	@Size(min = 1, max = 100, message = "q는 1자 이상 100자 이하여야 합니다")
	private String q;

	@Schema(description = "유튜브 카테고리 ID", required = false)
	private String category;

	@Schema(description = "최대 조회 개수 (1~300)", defaultValue = "100")
	@Min(value = 1, message = "maxResults는 최소 1 이상이어야 합니다")
	@Max(value = 300, message = "maxResults는 최대 300 이하여야 합니다")
	private long maxResults = 100;
}