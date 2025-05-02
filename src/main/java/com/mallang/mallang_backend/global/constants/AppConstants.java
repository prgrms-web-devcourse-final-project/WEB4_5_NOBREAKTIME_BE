package com.mallang.mallang_backend.global.constants;

public class AppConstants {

    /**
     * 토큰 값 설정
     */
    public static final String ACCESS_TOKEN = "access_token";
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
	/**
	 * 기본 단어장 이름
	 */
	public static final String DEFAULT_WORDBOOK_NAME = "기본";
    /**
     *  필터링 제외 경로 패턴
     */
    public static final String[] EXCLUDE_PATH_PATTERNS = {
            "/login/oauth2/code/**",
            "/api/v1/member/**",
            "/oauth2/**",
            "/api/test/login",
            "/health",
            "/env"
    };
    /**
     * 정적 리소스 확장자 패턴
     */
    public static final String STATIC_RESOURCES_REGEX = ".*\\.(css|js|gif|png|jpg|ico)$";
    /**
     * 소셜 로그인 사용자 정보 값
     */
    public static final String ID_KEY = "id";
    public static final String NICKNAME_KEY = "nickname";
    public static final String PROFILE_IMAGE_KEY = "profile_image";

	/**
	 * 영상 학습 퀴즈 최대 개수
	 */
	public static final int MAX_VIDEO_LEARNING_QUIZ_ITEMS = 5;

}
