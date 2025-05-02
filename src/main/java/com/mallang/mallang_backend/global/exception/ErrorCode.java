package com.mallang.mallang_backend.global.exception;

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
    TOKEN_NOT_FOUND("401-2", "token.not.found", HttpStatus.UNAUTHORIZED),
    IN_BLACKLIST("403-1", "in.blacklist", HttpStatus.FORBIDDEN),

    // Audio Errors
    AUDIO_DOWNLOAD_FAILED("500-1", "audio.download.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    AUDIO_FILE_NOT_FOUND("404-2", "audio.file.not.found", HttpStatus.NOT_FOUND),

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
    WORD_PARSE_FAILED("500-3", "word.parse.failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // ExpressionBook Errors
    EXPRESSION_BOOK_NOT_FOUND("404-1", "expression.book.not.found", HttpStatus.NOT_FOUND),
    FORBIDDEN_EXPRESSION_BOOK("403-1", "expression.book.forbidden", HttpStatus.FORBIDDEN),

    // Expression Errors
    EXPRESSION_NOT_FOUND("404-2", "expression.not.found", HttpStatus.NOT_FOUND),

    // Wordbook Errors
    // 해당 단어장이 없거나 권한이 없음
    NO_WORDBOOK_EXIST_OR_FORBIDDEN("403-1", "no.wordbook.exist.or.forbidden", HttpStatus.FORBIDDEN),
    // 단어장을 만들 권한이 없음(구독 플랜)
    NO_WORDBOOK_CREATE_PERMISSION("403-1", "wordbook.create.failed", HttpStatus.FORBIDDEN),
    // 기본 댠어장과 동일한 이름의 단어장을 생성 실패
    WORDBOOK_CREATE_DEFAULT_FORBIDDEN("403-1", "wordbook.create.default.forbidden", HttpStatus.FORBIDDEN),
    // 기본 단어장과 동일한 이름의 단어장으로 이름 변경 실패 또는 기본 단어장 이름 변경 실패
    WORDBOOK_RENAME_DEFAULT_FORBIDDEN("403-1", "wordbook.rename.default.forbidden", HttpStatus.FORBIDDEN),
    // 기본 단어장은 삭제할 수 없음
    WORDBOOK_DELETE_DEFAULT_FORBIDDEN("403-1", "wordbook.delete.default.forbidden", HttpStatus.FORBIDDEN),
    // 단어장에 해당 단어가 없음
    WORDBOOK_ITEM_NOT_FOUND("404-1", "wordbook.item.not.found", HttpStatus.NOT_FOUND),

    // Parse Errors
    INVALID_ATTRIBUTE_MAP("400-2", "invalid.attribute.map", HttpStatus.BAD_REQUEST),

    // Word Quiz Errors
    // 단어장에 단어가 없습니다.
    WORDBOOK_IS_EMPTY("400-1", "wordbook.is.empty", HttpStatus.BAD_REQUEST),
    // 퀴즈를 찾을 수 없음
    WORDQUIZ_NOT_FOUND("404-1", "wordquiz.not.found", HttpStatus.NOT_FOUND),
    // 퀴즈 생성에 가능한 단어가 부족합니다.
    NOT_ENOUGH_WORDS_FOR_QUIZ("400-1", "not.enough.words.for.quiz", HttpStatus.BAD_REQUEST),

    // 공통 API 에러 (fallback 처리용)
    API_ERROR("500-1", "api.error", HttpStatus.INTERNAL_SERVER_ERROR);


    private final String code;
    private final String messageCode; // 메시지 프로퍼티
    private final HttpStatus status;
}
