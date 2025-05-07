package com.mallang.mallang_backend.domain.video.video.service;

import java.io.IOException;
import java.util.List;

import com.mallang.mallang_backend.domain.video.video.dto.AnalyzeVideoResponse;
import com.mallang.mallang_backend.domain.video.video.dto.VideoDetail;
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

	VideoDetail fetchDetail(String videoId);

	VideoDetail getVideoDetail(String videoId);

	AnalyzeVideoResponse analyzeVideo(Long memberId, String videoID) throws IOException, InterruptedException;
}
