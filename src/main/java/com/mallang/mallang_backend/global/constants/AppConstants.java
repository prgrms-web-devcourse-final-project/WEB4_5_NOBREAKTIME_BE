package com.mallang.mallang_backend.global.constants;

import java.time.format.DateTimeFormatter;

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
    public static final String AUDIO_FILE_EXTENSION = ".webm";
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
     * 기본 표현함 이름
     */
    public static final String DEFAULT_EXPRESSION_BOOK_NAME = "기본";
    /**
     *  필터링 제외 경로 패턴
     */
    public static final String[] EXCLUDE_PATH_PATTERNS = {
            "/login/oauth2/code/**",
            "/oauth2/**",
            "/health",
            "/env",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/test",
            "/actuator/**",
            "/api/auth/test",
            "/api/v1/plans/**"
    };
    /**
     * 정적 리소스 확장자 패턴
     */
    public static final String STATIC_RESOURCES_REGEX = ".*\\.(css|js|gif|png|jpg|ico)$";
    /**
     * 소셜 로그인 사용자 정보 값
     */
    public static final String EMAIL_KEY = "email";
    public static final String PLATFORM_ID_KEY = "platformId";
    public static final String NICKNAME_KEY = "nickname";
    public static final String PROFILE_IMAGE_KEY = "profile_image";

	/**
	 * 비디오 히스토리 최대 갯수
	 */
	public static final int MAX_HISTORY_PER_MEMBER = 50;

    /**
     * s3 에 업로드 할 프로필 사진의 prefix
     */
    public static final String IMAGE_PREFIX_KEY = "profile-images";
    public static final String IMAGE_TYPE_KEY = "image/jpeg";

    /**
     * 자동 가입 시 닉네임 중복 처리 용도
     */
    public static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";

    /**
     * redis 저장용 key prefix
     */
    public static final String REFRESH_TOKEN_PREFIX = "refreshToken:";
    public static final String ORDER_ID_PREFIX = "order:";
    public static final String IDEM_KEY_PREFIX = "idempotencyKey:";
    public static final String BILLING_KEY_PREFIX = "billing:";

    /**
     * 주문 ID 생성 용도
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

	/**
	 * Redis Lock TTL (Time To Live)
	 */
	public static final long LOCK_TTL_MS      = 30_000L;   // 30초
	public static final long WAIT_INTERVAL_MS = 500L;      // 500ms

	/**
	 * 동영상 가져올 갯수
	 */
	public static final long CACHE_SCHEDULER_FETCH_SIZE = 300L;

	/**
	 * SSE Heartbeat Interval (초 단위)
	 */
	public static final long HEARTBEAT_INTERVAL_SEC = 15;
}
