package com.mallang.mallang_backend.domain.video.util;

import com.google.api.services.youtube.model.Video;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;

import java.util.Optional;

/**
 * Video 관련 공통 유틸리티 클래스
 */
public final class VideoUtils {

	// 인스턴스화 방지
	private VideoUtils() {}
	/**
	 * 영상의 기본 오디오 언어가 지정한 언어와 일치하는지 확인
	 */
	public static boolean matchesLanguage(Video video, String langKey) {
		return Optional.ofNullable(video.getSnippet())
			.map(snip -> langKey.equals(snip.getDefaultAudioLanguage()))
			.orElse(false);
	}

	/**
	 * VideoResponse DTO로 매핑 (Setter 사용)
	 */
	public static VideoResponse toVideoResponse(Video video) {
		var snip = video.getSnippet();
		VideoResponse dto = new VideoResponse();
		dto.setVideoId(video.getId());
		dto.setTitle(snip.getTitle());
		dto.setDescription(snip.getDescription());
		dto.setThumbnailUrl(snip.getThumbnails().getMedium().getUrl());
		dto.setDuration(video.getContentDetails().getDuration());
		return dto;
	}

	/**
	 * VideoResponse DTO로 매핑 (Setter 사용)
	 * 북마크 여부 추가
	 */
	public static VideoResponse toVideoResponse(Video video, boolean isBookmarked) {
		var snip = video.getSnippet();
		VideoResponse dto = new VideoResponse();
		dto.setVideoId(video.getId());
		dto.setTitle(snip.getTitle());
		dto.setDescription(snip.getDescription());
		dto.setThumbnailUrl(snip.getThumbnails().getMedium().getUrl());
		dto.setBookmarked(isBookmarked);
		dto.setDuration(video.getContentDetails().getDuration());
		return dto;
	}
}

