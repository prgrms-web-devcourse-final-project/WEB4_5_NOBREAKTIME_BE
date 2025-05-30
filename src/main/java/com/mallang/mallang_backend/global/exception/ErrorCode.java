package com.mallang.mallang_backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 멤버 에러
    MEMBER_NOT_FOUND("404-1", "member.not.found", HttpStatus.NOT_FOUND),
    MEMBER_ALREADY_WITHDRAWN("410-1", "member.already.withdrawn", HttpStatus.GONE),
    LANGUAGE_NOT_CONFIGURED("400-1", "language.not.configured", HttpStatus.BAD_REQUEST),
    DUPLICATE_FILED("409-1", "duplicate.filed", HttpStatus.CONFLICT),
    LANGUAGE_ALREADY_SET("409-1", "language.already.set", HttpStatus.CONFLICT),
    NICKNAME_GENERATION_FAILED("500-1", "nickname.generation.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    MEMBER_ALREADY_JOINED("409-2", "member.already.joined", HttpStatus.CONFLICT),

    // 구독 에러
    SUBSCRIPTION_ALREADY_EXISTS("409-3", "subscription.already.exists", HttpStatus.CONFLICT),

    // 토큰 에러
    TOKEN_EXPIRED("401-3", "token.expired", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FOUND("401-3", "token.not.found", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("401-4", "invalid.token", HttpStatus.UNAUTHORIZED),

    // 음성 에러
    AUDIO_DOWNLOAD_FAILED("500-4", "audio.download.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    TOO_MANY_CONCURRENT_AUDIO_EXTRACTIONS("500-5", "too.many.concurrent.audio.extractions", HttpStatus.INTERNAL_SERVER_ERROR),

    // 비디오 에러
    VIDEO_LENGTH_EXCEED("400-5", "video.length.exceed", HttpStatus.BAD_REQUEST),
    VIDEO_RETRIEVAL_FAILED("404-5", "video.retrieval.failed", HttpStatus.NOT_FOUND),
    VIDEO_PATH_CREATION_FAILED("500-5", "upload.path.creation.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    VIDEO_ID_SEARCH_FAILED("500-6", "video.id.search.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    VIDEO_DETAIL_FETCH_FAILED("500-7", "video.detail.fetch.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    ANALYZE_VIDEO_CONCURRENCY_TIME_OUT("500-8", "analyze.video.concurrency.time.out", HttpStatus.INTERNAL_SERVER_ERROR),
    ANALYZE_VIDEO_FAILED("500-9", "analyze.video.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    CATEGORY_NOT_FOUND("404-6", "category.not.found", HttpStatus.NOT_FOUND),
    VIDEO_ANALYSIS_FAILED("500-10", "video.analysis.failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // GPT 에러
    GPT_RESPONSE_PARSE_FAIL("500-11", "gpt.response.parse.fail", HttpStatus.INTERNAL_SERVER_ERROR),
    GPT_API_CALL_FAILED("500-12", "gpt.api.call.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    GPT_RESPONSE_EMPTY("500-13", "gpt.response.empty", HttpStatus.INTERNAL_SERVER_ERROR),

    // 단어, 단어 저장 에러
    WORD_SAVE_FAILED("500-14", "word.save.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    WORD_PARSE_FAILED("500-15", "word.parse.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    WORD_NOT_FOUND("404-14", "word.not.found", HttpStatus.NOT_FOUND),
    SAVED_WORD_CONCURRENCY_TIME_OUT("500-16", "saved.word.concurrency.time.out", HttpStatus.INTERNAL_SERVER_ERROR),
    LANGUAGE_MISMATCH("400-14", "language.mismatch", HttpStatus.BAD_REQUEST),
    INVALID_WORD("500-17", "invalid.word", HttpStatus.INTERNAL_SERVER_ERROR),

    // 단어장 에러
    // 해당 단어장이 없거나 권한이 없음
    NO_WORDBOOK_EXIST_OR_FORBIDDEN("403-20", "no.wordbook.exist.or.forbidden", HttpStatus.FORBIDDEN),
    // 단어장을 만들 권한이 없음(구독 플랜)
    NO_WORDBOOK_CREATE_PERMISSION("403-21", "wordbook.create.failed", HttpStatus.FORBIDDEN),
    // 기본 단어장과 동일한 이름의 단어장을 생성 실패
    WORDBOOK_CREATE_DEFAULT_FORBIDDEN("403-22", "wordbook.create.default.forbidden", HttpStatus.FORBIDDEN),
    // 기본 단어장과 동일한 이름의 단어장으로 이름 변경 실패 또는 기본 단어장 이름 변경 실패
    WORDBOOK_RENAME_DEFAULT_FORBIDDEN("403-23", "wordbook.rename.default.forbidden", HttpStatus.FORBIDDEN),
    // 기본 단어장은 삭제할 수 없음
    WORDBOOK_DELETE_DEFAULT_FORBIDDEN("403-24", "wordbook.delete.default.forbidden", HttpStatus.FORBIDDEN),
    // 단어장 이름이 중복됨
    DUPLICATE_WORDBOOK_NAME("400-20", "wordbook.name.duplicate", HttpStatus.BAD_REQUEST),
    // 단어장에 해당 단어가 없음
    WORDBOOK_ITEM_NOT_FOUND("404-20", "wordbook.item.not.found", HttpStatus.NOT_FOUND),
    LANGUAGE_IS_NONE("400-21", "language.is.none", HttpStatus.BAD_REQUEST),
    NO_PERMISSION("403-25", "no.permission", HttpStatus.FORBIDDEN),
    // 단어장에 중복 단어 불가
    DUPLICATE_WORD_SAVED("400-22", "duplicate.word.saved", HttpStatus.BAD_REQUEST),

    // 표현함 에러
    EXPRESSION_BOOK_NOT_FOUND("404-18", "expression.book.not.found", HttpStatus.NOT_FOUND),
    FORBIDDEN_EXPRESSION_BOOK("403-18", "expression.book.forbidden", HttpStatus.FORBIDDEN),
    SUBTITLE_NOT_FOUND("404-18", "subtitle.not.found", HttpStatus.NOT_FOUND),

    // 표현 에러
    EXPRESSION_NOT_FOUND("404-19", "expression.not.found", HttpStatus.NOT_FOUND),

    // 파싱 에러
    INVALID_ATTRIBUTE_MAP("400-26", "invalid.attribute.map", HttpStatus.BAD_REQUEST),
    JSON_PARSE_ERROR("400-27", "json.parse.error", HttpStatus.BAD_REQUEST),

    /**
     * 단어 퀴즈 에러
     */
    // 단어장에 단어가 없음
    WORDBOOK_IS_EMPTY("400-28", "wordbook.is.empty", HttpStatus.BAD_REQUEST),
    // 퀴즈를 찾을 수 없음
    WORDQUIZ_NOT_FOUND("404-28", "wordquiz.not.found", HttpStatus.NOT_FOUND),
    // 퀴즈 생성에 가능한 단어가 부족합니다.
    NOT_ENOUGH_WORDS_FOR_QUIZ("400-29", "not.enough.words.for.quiz", HttpStatus.BAD_REQUEST),

    /**
     * 표현 퀴즈 에러
     */
    // 해당 표현함이 없거나 권한이 없음
    NO_EXPRESSION_BOOK_EXIST_OR_FORBIDDEN("403-30", "no.expressionbook.exist.or.forbidden", HttpStatus.FORBIDDEN),
    // 표현함에 표현이 없습니다.
    EXPRESSION_BOOK_IS_EMPTY("400-30", "expressionbook.is.empty", HttpStatus.BAD_REQUEST),
    // 표현함의 표현을 찾을 수 없음
    EXPRESSION_BOOK_ITEM_NOT_FOUND("404-30", "expressionbook.item.not.found", HttpStatus.NOT_FOUND),
    // 표현 퀴즈를 찾을 수 없음
    EXPRESSION_QUIZ_NOT_FOUND("404-31", "expressionquiz.not.found", HttpStatus.NOT_FOUND),
    // 표현함 생성 권한이 없음
    NO_EXPRESSION_BOOK_CREATE_PERMISSION("403-31", "expressionbook.create.failed", HttpStatus.FORBIDDEN),
    // 기본 표현함과 동일한 이름의 표현함을 생성 실패
    EXPRESSION_BOOK_CREATE_DEFAULT_FORBIDDEN("403-32", "expressionbook.create.default.forbidden", HttpStatus.FORBIDDEN),
    // 기본 표현함과 동일한 이름의 표현함으로 이름 변경 실패 또는 기본 표현함 이름 변경 실패
    EXPRESSION_BOOK_RENAME_DEFAULT_FORBIDDEN("403-33", "expressionbook.rename.default.forbidden", HttpStatus.FORBIDDEN),
    // 기본 표현함은 삭제할 수 없음
    EXPRESSION_BOOK_DELETE_DEFAULT_FORBIDDEN("403-34", "expressionbook.delete.default.forbidden", HttpStatus.FORBIDDEN),
    // 표현함 이름이 중복됨
    DUPLICATE_EXPRESSION_BOOK_NAME("400-31", "expressionbook.name.duplicate", HttpStatus.BAD_REQUEST),

    // 로그인 에러
    UNSUPPORTED_OAUTH_PROVIDER("404-35", "unsupported.oauth.provider", HttpStatus.NOT_FOUND),
    OAUTH_RATE_LIMIT("500-35", "oauth.rate.limit", HttpStatus.INTERNAL_SERVER_ERROR),
    LOCK_ACQUIRED_FAILED("423-35", "lock.acquired.failed", HttpStatus.LOCKED),
    REJOIN_BLOCKED("403-35", "rejoin.blocked", HttpStatus.FORBIDDEN),

    // 파일 업로드 에러
    FILE_UPLOAD_FAILED("500-36", "file.upload.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_EXIST_BUCKET("404-36", "not.exist.bucket", HttpStatus.NOT_FOUND),
    FILE_EMPTY("404-37", "empty.file", HttpStatus.NOT_FOUND),
    NOT_SUPPORTED_TYPE("400-36", "not.supported.type", HttpStatus.BAD_REQUEST),

    // redirect Errors
    REDIRECTION_FAILED("500-38", "redirection.failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // 공통 API 에러 (fallback 처리용)
    API_ERROR("500-39", "api.error", HttpStatus.INTERNAL_SERVER_ERROR),
    API_BLOCK("500-40", "api.block", HttpStatus.INTERNAL_SERVER_ERROR),

    // 영상 학습 퀴즈용 에러
    KEYWORD_NOT_FOUND("404-41", "keyword.not.found", HttpStatus.NOT_FOUND),

    // 북마크 에러
    BOOKMARK_ALREADY_EXISTS("409-42", "bookmark.already.exists", HttpStatus.CONFLICT),
    BOOKMARK_NOT_FOUND("404-43", "bookmark.not.found", HttpStatus.NOT_FOUND),

    // 결제 상황 오류
    PLAN_NOT_FOUND("404-44", "plan.not.found", HttpStatus.NOT_FOUND),
    PAYMENT_NOT_FOUND("404-45", "payment.not.found", HttpStatus.NOT_FOUND),
    MISSING_BILLING_KEY("404-46", "missing.billing.key", HttpStatus.NOT_FOUND),
    NOT_FOUND_MEMBER_GRANTED_INFO("404-47", "not.found.member.granted.info", HttpStatus.NOT_FOUND),

    CONNECTION_FAIL("500-44", "connection.fail", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_CONFIRM_FAIL("500-45", "payment.confirm.fail", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_PROCESSING_PREPARED_FAILED("500-46", "payment.processing.prepared.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_PROCESSING_RESULT_SAVED_FAILED("500-47", "payment.processing.result.saved.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    REDIS_CONNECTION_FAILED("500-48", "redis.connection.failed", HttpStatus.INTERNAL_SERVER_ERROR),

    PAYMENT_CONFLICT("409-44", "order.id.conflict", HttpStatus.CONFLICT),
    PAYMENT_AMOUNT_MISMATCH("409-45", "payment.rejected", HttpStatus.CONFLICT),

    BILLING_PAYMENT_FAIL("400-44", "billing.payment.fail", HttpStatus.BAD_REQUEST),
    BILLING_KEY_ISSUE_FAILED("400-45", "billing.key.issue.failed", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_STATUS("400-46", "invalid.payment.status", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_STATE("400-47", "invalid.payment.state", HttpStatus.BAD_REQUEST),

    ORDER_AMOUNT_MISMATCH("422-44", "order.amount.mismatch", HttpStatus.UNPROCESSABLE_ENTITY),
    SUBSCRIPTION_NOT_FOUND("404-48", "subscription.not.found", HttpStatus.NOT_FOUND),

    // 스케줄링 오류
    INVALID_CRON_EXPRESSION_EXCEPTION_CODE("400-49", "invalid.cron.expression", HttpStatus.BAD_REQUEST),
    SUBSCRIPTION_STATUS_UPDATE_FAILED("500-50", "subscription.status.update.failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // 메일 전송 에러
    EMAIL_SEND_FAILED("500-51", "email.send.failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // 학습 레벨 측정 에러
    LEVEL_NOT_MEASURABLE("400-52", "level.not.measurable", HttpStatus.BAD_REQUEST),
    LEVEL_PARSE_FAILED("500-53", "level.parse.failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // 캐시 관련 에러
    CACHE_LOCK_TIMEOUT("500-54", "cache.lock.timeout", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_LANGUAGE("400-55", "invalid.language", HttpStatus.BAD_REQUEST);


    private final String code;
    private final String messageCode; // 메시지 프로퍼티
    private final HttpStatus status;
}
