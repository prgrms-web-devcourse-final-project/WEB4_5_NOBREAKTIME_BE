package com.mallang.mallang_backend.domain.video.video.service;

import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;

import java.util.List;

import java.io.IOException;

public interface VideoService {
    List<VideoResponse> getVideosByLanguage(
        String q,
        String category,
        String language,
        long maxResults
    );
	String analyzeVideo(String videoUrl) throws IOException, InterruptedException;

	byte[] getAudioFile(String fileName) throws IOException;
}
