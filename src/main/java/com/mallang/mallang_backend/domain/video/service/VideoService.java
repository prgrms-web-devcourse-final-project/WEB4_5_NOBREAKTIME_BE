package com.mallang.mallang_backend.domain.video.service;

import java.io.IOException;

public interface VideoService {
	String analyzeVideo(String videoUrl) throws IOException, InterruptedException;

	byte[] getAudioFile(String fileName) throws IOException;
}
