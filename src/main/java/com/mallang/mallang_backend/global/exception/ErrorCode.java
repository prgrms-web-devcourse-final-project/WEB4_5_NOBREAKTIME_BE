package com.mallang.mallang_backend.global.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /**
     * 사용자를 찾을 수 없음
     */
    USER_NOT_FOUND("404-1", "user.not.found", HttpStatus.NOT_FOUND),

    // Token Errors
    TOKEN_EXPIRED("401-1", "token.expired", HttpStatus.UNAUTHORIZED),

    // Audio Errors
    AUDIO_DOWNLOAD_FAILED("500-1", "audio.download.failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // Video Errors
    VIDEO_LENGTH_EXCEED("400-1", "video.length.exceed", HttpStatus.BAD_REQUEST),
    VIDEO_RETRIEVAL_FAILED("404-1", "video.retrieval.failed", HttpStatus.NOT_FOUND),
    VIDEO_PATH_CREATION_FAILED("500-1", "upload.path.creation.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    VIDEO_ID_SEARCH_FAILED("500-1", "video.id.search.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    VIDEO_DETAIL_FETCH_FAILED("500-2", "video.detail.fetch.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    // GPT Errors
    GPT_RESPONSE_PARSE_FAIL("500-1", "gpt.response.parse.fail", HttpStatus.INTERNAL_SERVER_ERROR),
    GPT_API_CALL_FAILED("500-4", "gpt.api.call.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    GPT_RESPONSE_EMPTY("500-5", "gpt.response.empty", HttpStatus.INTERNAL_SERVER_ERROR),

    // Word Errors
    WORD_SAVE_FAILED("500-2", "word.save.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    WORD_PARSE_FAILED("500-3", "word.parse.failed", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String messageCode; // 메시지 프로퍼티
    private final HttpStatus status;
}
