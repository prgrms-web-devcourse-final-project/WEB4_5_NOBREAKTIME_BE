package com.mallang.mallang_backend.domain.video.video.service;

import java.io.IOException;
import java.util.List;

import com.mallang.mallang_backend.domain.video.video.dto.AnalyzeVideoResponse;
import com.mallang.mallang_backend.domain.video.video.dto.VideoDetailResponse;
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

	VideoDetailResponse fetchDetail(String videoId);

	VideoDetailResponse getVideoDetail(String videoId);

	AnalyzeVideoResponse analyzeVideo(String videoID) throws IOException, InterruptedException;

	byte[] getAudioFile(String fileName) throws IOException;
}
