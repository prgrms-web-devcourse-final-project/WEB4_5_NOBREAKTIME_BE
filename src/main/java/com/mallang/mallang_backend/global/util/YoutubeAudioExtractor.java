package com.mallang.mallang_backend.global.util;

import java.io.IOException;

public interface YoutubeAudioExtractor {
	/**
	 * 유튜브 링크를 받아서 음성 파일(mp3)로 변환하고 파일 경로를 반환한다.
	 * @param youtubeUrl 유튜브 영상 URL
	 * @return 음성 파일 경로
	 */
	String extractAudio(String youtubeUrl) throws IOException, InterruptedException;
}