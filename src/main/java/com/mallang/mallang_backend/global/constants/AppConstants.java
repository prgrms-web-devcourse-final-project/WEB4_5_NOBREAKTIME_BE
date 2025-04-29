package com.mallang.mallang_backend.global.constants;

public class AppConstants {

    public static final String REFRESH_TOKEN = "refresh_token";
    /**
     * 영상 길이 제한
     */
    public static final int VIDEO_LENGTH_LIMIT_SECONDS = 1200;
    /**
     * 음성 파일 이름 prefix
     */
    public static final String AUDIO_FILE_PREFIX = "audio_";
    /**
     * 음성 파일 타입
     */
    public static final String AUDIO_FILE_EXTENSION = ".mp3";
    /**
     * 파일 위치 (현재 프로젝트 디렉토리)
     */
    public static final String UPLOADS_DIR = System.getProperty("user.dir") + "/uploads/";
    /**
     * Youtube Video ID 로 전체 URL 을 만들기 위한 Youtbe 영상 Base URL
     */
    public static final String YOUTUBE_VIDEO_BASE_URL = "https://www.youtube.com/watch?v=";
	/**
	 * Youtube 라이센스 타입
	 */
    public static final String CC_LICENSE = "creativeCommon";
}
