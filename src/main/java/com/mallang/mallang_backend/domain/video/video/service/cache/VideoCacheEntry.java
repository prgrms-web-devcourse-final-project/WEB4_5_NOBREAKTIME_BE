package com.mallang.mallang_backend.domain.video.video.service.cache;

import java.util.List;

import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;

import lombok.Getter;

@Getter
public class VideoCacheEntry {
	private List<VideoResponse> videos;
	private long fetchedMaxResults;

	// 기본 생성자(직렬화용)
	public VideoCacheEntry() {}

	public VideoCacheEntry(List<VideoResponse> videos, long fetchedMaxResults) {
		this.videos = videos;
		this.fetchedMaxResults = fetchedMaxResults;
	}

}