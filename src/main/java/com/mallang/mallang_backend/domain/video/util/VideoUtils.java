package com.mallang.mallang_backend.domain.video.util;

import java.time.Duration;
import java.util.Optional;

import com.google.api.services.youtube.model.Video;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;

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
	 * ISO-8601 기간 문자열을 H:mm:ss 또는 mm:ss 형태로 포맷
	 */
	public static String formatDuration(String isoDuration) {
		Duration d = Duration.parse(isoDuration);
		long seconds = d.getSeconds();
		long hours = seconds / 3600;
		long minutes = (seconds % 3600) / 60;
		long secs = seconds % 60;
		if (hours > 0) {
			return String.format("%d:%02d:%02d", hours, minutes, secs);
		} else {
			return String.format("%02d:%02d", minutes, secs);
		}
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
		dto.setDuration(formatDuration(video.getContentDetails().getDuration()));
		return dto;
	}
}

