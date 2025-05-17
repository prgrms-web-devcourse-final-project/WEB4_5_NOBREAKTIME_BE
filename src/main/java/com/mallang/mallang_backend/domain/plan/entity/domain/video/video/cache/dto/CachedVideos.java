package com.mallang.mallang_backend.domain.plan.entity.domain.video.video.cache.dto;

import com.mallang.mallang_backend.domain.plan.entity.domain.video.video.dto.VideoResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CachedVideos {
	/** 캐시에 저장된 최대 fetchSize */
	private long rawFetchSize;
	/** 실제 필터링된 VideoResponse 리스트 */
	private List<VideoResponse> responses;
}
