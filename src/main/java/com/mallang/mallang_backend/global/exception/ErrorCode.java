package com.mallang.mallang_backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Member Errors
    MEMBER_NOT_FOUND("404-1", "member.not.found", HttpStatus.NOT_FOUND), //  사용자를 찾을 수 없음
    MEMBER_ALREADY_WITHDRAWN("410-1", "member.already.withdrawn", HttpStatus.GONE),
    LANGUAGE_NOT_CONFIGURED("400-6", "language.not.configured", HttpStatus.BAD_REQUEST), // 언어 설정이 되어 있지 않음
    DUPLICATE_FILED("409-1", "duplicate.filed", HttpStatus.CONFLICT),
    NOT_CHANGED("400-9", "not.changed", HttpStatus.BAD_REQUEST),
    LANGUAGE_ALREADY_SET("410-9", "language.already.set", HttpStatus.CONFLICT),
    CANNOT_SIGNUP_WITH_THIS_ID("409-2", "cannot.signup.with.this.id", HttpStatus.UNAUTHORIZED),
    NICKNAME_GENERATION_FAILED("500-1", "nickname.generation.failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // Token Errors
    TOKEN_EXPIRED("401-1", "token.expired", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FOUND("401-2", "token.not.found", HttpStatus.UNAUTHORIZED),
    IN_BLACKLIST("403-1", "in.blacklist", HttpStatus.FORBIDDEN),
    INVALID_TOKEN("401-3", "invalid.token", HttpStatus.UNAUTHORIZED),

    // Audio Errors
    AUDIO_DOWNLOAD_FAILED("500-1", "audio.download.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    AUDIO_FILE_NOT_FOUND("404-2", "audio.file.not.found", HttpStatus.NOT_FOUND),
    TOO_MANY_CONCURRENT_AUDIO_EXTRACTIONS("500-1", "too.many.concurrent.audio.extractions", HttpStatus.INTERNAL_SERVER_ERROR),

    // Video Errors
    VIDEO_LENGTH_EXCEED("400-1", "video.length.exceed", HttpStatus.BAD_REQUEST),
    VIDEO_RETRIEVAL_FAILED("404-1", "video.retrieval.failed", HttpStatus.NOT_FOUND),
    VIDEO_PATH_CREATION_FAILED("500-1", "upload.path.creation.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    VIDEO_ID_SEARCH_FAILED("500-1", "video.id.search.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    VIDEO_DETAIL_FETCH_FAILED("500-2", "video.detail.fetch.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    ANALYZE_VIDEO_CONCURRENCY_TIME_OUT("500-4", "analyze.video.concurrency.time.out", HttpStatus.INTERNAL_SERVER_ERROR),
    ANALYZE_VIDEO_FAILED("500-5", "analyze.video.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    CATEGORY_NOT_FOUND("404-2", "category.not.found", HttpStatus.NOT_FOUND),
    VIDEO_ANALYSIS_FAILED("500-6", "video.analysis.failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // GPT Errors
    GPT_RESPONSE_PARSE_FAIL("500-1", "gpt.response.parse.fail", HttpStatus.INTERNAL_SERVER_ERROR),
    GPT_API_CALL_FAILED("500-4", "gpt.api.call.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    GPT_RESPONSE_EMPTY("500-5", "gpt.response.empty", HttpStatus.INTERNAL_SERVER_ERROR),

    // Word Errors
    WORD_SAVE_FAILED("500-2", "word.save.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    WORD_PARSE_FAILED("500-3", "word.parse.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    WORD_NOT_FOUND("404-1", "word.not.found", HttpStatus.NOT_FOUND),
    SAVED_WORD_CONCURRENCY_TIME_OUT("500-4", "saved.word.concurrency.time.out", HttpStatus.INTERNAL_SERVER_ERROR),
    LANGUAGE_MISMATCH("400-1", "language.mismatch", HttpStatus.BAD_REQUEST),
    INVALID_WORD("500-5", "invalid.word", HttpStatus.INTERNAL_SERVER_ERROR),

    // ExpressionBook Errors
    EXPRESSION_BOOK_NOT_FOUND("404-1", "expression.book.not.found", HttpStatus.NOT_FOUND),
    FORBIDDEN_EXPRESSION_BOOK("403-1", "expression.book.forbidden", HttpStatus.FORBIDDEN),
    SUBTITLE_NOT_FOUND("404-1", "subtitle.not.found", HttpStatus.NOT_FOUND),

    // Expression Errors
    EXPRESSION_NOT_FOUND("404-2", "expression.not.found", HttpStatus.NOT_FOUND),

    // Wordbook Errors
    // 해당 단어장이 없거나 권한이 없음
    NO_WORDBOOK_EXIST_OR_FORBIDDEN("403-1", "no.wordbook.exist.or.forbidden", HttpStatus.FORBIDDEN),    // 해당 단어장이 없거나 권한이 없음
    // 단어장을 만들 권한이 없음(구독 플랜)
    NO_WORDBOOK_CREATE_PERMISSION("403-1", "wordbook.create.failed", HttpStatus.FORBIDDEN),
    // 기본 댠어장과 동일한 이름의 단어장을 생성 실패
    WORDBOOK_CREATE_DEFAULT_FORBIDDEN("403-1", "wordbook.create.default.forbidden", HttpStatus.FORBIDDEN),
    // 기본 단어장과 동일한 이름의 단어장으로 이름 변경 실패 또는 기본 단어장 이름 변경 실패
    WORDBOOK_RENAME_DEFAULT_FORBIDDEN("403-1", "wordbook.rename.default.forbidden", HttpStatus.FORBIDDEN),
    // 기본 단어장은 삭제할 수 없음
    WORDBOOK_DELETE_DEFAULT_FORBIDDEN("403-1", "wordbook.delete.default.forbidden", HttpStatus.FORBIDDEN),
    // 단어장 이름이 중복됨
    DUPLICATE_WORDBOOK_NAME("400-2", "wordbook.name.duplicate", HttpStatus.BAD_REQUEST),
    // 단어장에 해당 단어가 없음
    WORDBOOK_ITEM_NOT_FOUND("404-1", "wordbook.item.not.found", HttpStatus.NOT_FOUND),
    LANGUAGE_IS_NONE("400-1", "language.is.none", HttpStatus.BAD_REQUEST),
    NO_PERMISSION("403-1", "no.permission", HttpStatus.FORBIDDEN),
    // 단어장에 중복 단어 불가
    DUPLICATE_WORD_SAVED("400-2", "duplicate.word.saved", HttpStatus.BAD_REQUEST),

    // Parse Errors
    INVALID_ATTRIBUTE_MAP("400-2", "invalid.attribute.map", HttpStatus.BAD_REQUEST),

    // Word Quiz Errors
    // 단어장에 단어가 없습니다.
    WORDBOOK_IS_EMPTY("400-1", "wordbook.is.empty", HttpStatus.BAD_REQUEST),
    // 퀴즈를 찾을 수 없음
    WORDQUIZ_NOT_FOUND("404-1", "wordquiz.not.found", HttpStatus.NOT_FOUND),
    // 퀴즈 생성에 가능한 단어가 부족합니다.
    NOT_ENOUGH_WORDS_FOR_QUIZ("400-1", "not.enough.words.for.quiz", HttpStatus.BAD_REQUEST),

    // Epression Quiz Errors
    // 해당 표현함이 없거나 권한이 없음
    NO_EXPRESSIONBOOK_EXIST_OR_FORBIDDEN("403-1", "no.expressionbook.exist.or.forbidden", HttpStatus.FORBIDDEN),
    // 표현함에 표현이 없습니다.
    EXPRESSIONBOOK_IS_EMPTY("400-1", "expressionbook.is.empty", HttpStatus.BAD_REQUEST),
    // 표현함의 표현을 찾을 수 없음
    EXPRESSIONBOOK_ITEM_NOT_FOUND("404-1", "expressionbook.item.not.found", HttpStatus.NOT_FOUND),
    // 표현 퀴즈를 찾을 수 없음
    EXPRESSIONQUIZ_NOT_FOUND("404-1", "expressionquiz.not.found", HttpStatus.NOT_FOUND),
    // 표현함 생성 권한이 없음
    NO_EXPRESSIONBOOK_CREATE_PERMISSION("403-1", "expressionbook.create.failed", HttpStatus.FORBIDDEN),
    // 기본 표현함과 동일한 이름의 표현함을 생성 실패
    EXPRESSIONBOOK_CREATE_DEFAULT_FORBIDDEN("403-1", "expressionbook.create.default.forbidden", HttpStatus.FORBIDDEN),
    // 기본 표현함과 동일한 이름의 표현함으로 이름 변경 실패 또는 기본 표현함 이름 변경 실패
    EXPRESSIONBOOK_RENAME_DEFAULT_FORBIDDEN("403-1", "expressionbook.rename.default.forbidden", HttpStatus.FORBIDDEN),
    // 기본 표현함은 삭제할 수 없음
    EXPRESSIONBOOK_DELETE_DEFAULT_FORBIDDEN("403-1", "expressionbook.delete.default.forbidden", HttpStatus.FORBIDDEN),
    // 표현함 이름이 중복됨
    DUPLICATE_EXPRESSIONBOOK_NAME("400-2", "expressionbook.name.duplicate", HttpStatus.BAD_REQUEST),

    // login Errors
    UNSUPPORTED_OAUTH_PROVIDER("404-2", "unsupported.oauth.provider", HttpStatus.NOT_FOUND),
    OAUTH_NETWORK_ERROR("500-1", "oauth.network.error", HttpStatus.INTERNAL_SERVER_ERROR),
    OAUTH_RATE_LIMIT("500-1", "oauth.rate.limit", HttpStatus.INTERNAL_SERVER_ERROR),
    PROFILE_NOT_FOUND("404-2", "profile.not.found", HttpStatus.NOT_FOUND),

    // file upload Errors
    FILE_UPLOAD_FAILED("500-7", "file.upload.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_EXIST_BUCKET("404-3", "not.exist.bucket", HttpStatus.NOT_FOUND),
    FILE_EMPTY("404-4", "empty.file", HttpStatus.NOT_FOUND),
    NOT_SUPPORTED_TYPE("400-5", "not.supported.type", HttpStatus.BAD_REQUEST),

    // redirect Errors
    REDIRECTION_FAILED("500-8", "redirection.failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // 공통 API 에러 (fallback 처리용)
    API_ERROR("500-1", "api.error", HttpStatus.INTERNAL_SERVER_ERROR),
    API_BLOCK("500-2", "api.block", HttpStatus.INTERNAL_SERVER_ERROR),

    // 영상 학습 퀴즈용 에러
    KEYWORD_NOT_FOUND("404-1", "keyword.not.found", HttpStatus.NOT_FOUND),

    // 북마크 에러
    BOOKMARK_ALREADY_EXISTS("409-2", "bookmark.already.exists", HttpStatus.CONFLICT),
    BOOKMARK_NOT_FOUND("404-3", "bookmark.not.found", HttpStatus.NOT_FOUND),

    // 결제 상황 오류
    PLAN_NOT_FOUND("404-2", "plan.not.found", HttpStatus.NOT_FOUND),
    CONNECTION_FAIL("500-3", "connection.fail", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_NOT_FOUND("404-4", "payment.not.found", HttpStatus.NOT_FOUND),
    PAYMENT_CONFLICT("409-5", "order.id.conflict", HttpStatus.CONFLICT),
    PAYMENT_AMOUNT_MISMATCH("409-6", "payment.rejected", HttpStatus.CONFLICT),
    PAYMENT_CONFIRM_FAIL("500-4", "payment.confirm.fail", HttpStatus.INTERNAL_SERVER_ERROR),
    BILLING_PAYMENT_FAIL("400-3", "billing.payment.fail", HttpStatus.BAD_REQUEST),
    SUBSCRIPTION_NOT_FOUND("404-5", "subscription.not.found", HttpStatus.NOT_FOUND),
    BILLING_KEY_ISSUE_FAILED("400-4", "billing.key.issue.failed", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_STATUS("400-5", "invalid.payment.status", HttpStatus.BAD_REQUEST),
    MISSING_BILLING_KEY("404-6", "missing.billing.key", HttpStatus.NOT_FOUND),
    INVALID_PAYMENT_STATE("400-6", "invalid.payment.state", HttpStatus.BAD_REQUEST),
    ORDER_AMOUNT_MISMATCH("422-1", "order.amount.mismatch", HttpStatus.UNPROCESSABLE_ENTITY),
    PAYMENT_PROCESSING_PREPARED_FAILED("500-5", "payment.processing.prepared.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_PROCESSING_RESULT_SAVED_FAILED("500-6", "payment.processing.result.saved.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND_MEMBER_GRANTED_INFO("404-7", "not.found.member.granted.info", HttpStatus.NOT_FOUND),

    // 스케줄링 오류
    INVALID_CRON_EXPRESSION_EXCEPTION_CODE("400-7", "invalid.cron.expression", HttpStatus.BAD_REQUEST),
    SUBSCRIPTION_STATUS_UPDATE_FAILED("500-2", "subscription.status.update.failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // 메일 전송 에러
    EMAIL_SEND_FAILED("500-5", "email.send.failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // 학습 레벨 측정 에러
    LEVEL_NOT_MEASURABLE("400-1", "level.not.measurable", HttpStatus.BAD_REQUEST),
    LEVEL_PARSE_FAILED("500-1", "level.parse.failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // 캐시 관련 에러
    CACHE_LOCK_TIMEOUT("500-6", "cache.lock.timeout", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_LANGUAGE("400-1", "invalid.language", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String messageCode; // 메시지 프로퍼티
    private final HttpStatus status;
}
