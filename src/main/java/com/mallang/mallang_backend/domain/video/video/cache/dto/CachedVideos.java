package com.mallang.mallang_backend.domain.video.video.cache.dto;

import java.util.List;

import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CachedVideos {
	/** 캐시에 저장된 최대 fetchSize */
	private long rawFetchSize;
	/** 실제 필터링된 VideoResponse 리스트 */
	private List<VideoResponse> responses;
}
