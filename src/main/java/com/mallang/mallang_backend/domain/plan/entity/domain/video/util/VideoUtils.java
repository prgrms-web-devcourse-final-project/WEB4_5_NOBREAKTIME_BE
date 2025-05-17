package com.mallang.mallang_backend.domain.plan.entity.domain.video.util;

import com.google.api.services.youtube.model.Video;
import com.mallang.mallang_backend.domain.plan.entity.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.plan.entity.domain.video.youtube.mapper.DurationMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.mallang.mallang_backend.global.constants.AppConstants.CC_LICENSE;

/**
 * Video 관련 공통 유틸리티 클래스
 */
public final class VideoUtils {

	// 인스턴스화 방지
	private VideoUtils() {}

	/**
	 * 영상 길이가 20분 이하인지 체크
	 */
	public static boolean isDurationLessThanOrEqualTo20Minutes(Video video) {
		String durationStr = video.getContentDetails().getDuration();
		Duration duration = DurationMapper.parseDuration(durationStr);
		return duration != null && duration.toMinutes() <= 20;
	}

	/**
	 * 크리에이티브 커먼즈 라이선스인지 확인
	 */
	public static boolean isCreativeCommons(Video video) {
		return Optional.ofNullable(video.getStatus())
			.map(status -> CC_LICENSE.equals(status.getLicense()))
			.orElse(false);
	}

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
		return dto;
	}

	/**
	 * 기본 검색(defaultSearch)일 때 리스트를 섞어서 반환
	 */
	public static List<VideoResponse> shuffleIfDefault(List<VideoResponse> list, boolean isDefault) {
		if (!isDefault) {
			return list;
		}
		List<VideoResponse> copy = new ArrayList<>(list);
		Collections.shuffle(copy);
		return copy;
	}
}

