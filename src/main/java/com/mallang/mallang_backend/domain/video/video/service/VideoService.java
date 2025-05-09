package com.mallang.mallang_backend.domain.video.video.service;

import java.io.IOException;
import java.util.List;

import com.mallang.mallang_backend.domain.video.video.dto.AnalyzeVideoResponse;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;

public interface VideoService {
    List<VideoResponse> getVideosByLanguage(
        String q,
        String category,
        String language,
        long maxResults
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
	 * @param memberId
	 * @param videoID 유튜브 영상 ID
	 * @return 영상 분석 결과
	 * @throws IOException 영상 음성 추출 실패
	 * @throws InterruptedException 영상 음성 추출 실패
	 */
	AnalyzeVideoResponse analyzeVideo(Long memberId, String videoID) throws IOException, InterruptedException;
}
