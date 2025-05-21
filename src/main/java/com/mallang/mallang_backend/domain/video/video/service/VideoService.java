package com.mallang.mallang_backend.domain.video.video.service;

import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface VideoService {
	List<VideoResponse> getVideosByLanguage(
			String q,
			String category,
			String language,
			long maxResults,
			Set<String> bookmarkedIds
	);

	List<VideoResponse> getVideosForMember(
			String q,
			String category,
			long maxResults,
			Long memberId
	);

	/**
	 * 유튜브 영상 ID로 영상 정보를 저장하고, 음성을 추출하고, STT 처리 후 시간별 스크립트와 핵심 단어를 추출합니다.
	 * 추출된 핵심 단어는 영상 분석이 끝난 후 비동기적으로 Word로 저장됩니다.
	 *
	 * @param memberId
	 * @param videoID  유튜브 영상 ID
	 * @param emitterId
	 * @throws IOException          영상 음성 추출 실패
	 * @throws InterruptedException 영상 음성 추출 실패
	 */
	void analyzeWithSseAsync(Long memberId, String videoID, String emitterId);

	Videos saveVideoIfAbsent(String videoId);
}