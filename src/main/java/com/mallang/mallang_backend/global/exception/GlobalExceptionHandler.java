package com.mallang.mallang_backend.global.exception;

import com.mallang.mallang_backend.global.exception.custom.LockAcquisitionException;
import com.mallang.mallang_backend.global.exception.message.MessageService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageService messageService;
    private final MeterRegistry meterRegistry;

    /**
     * 예시 응답
     * {
     * "timestamp": "2025-04-26T23:20:00",
     * "status": 404,
     * "code": "404-1",
     * "message": "사용자를 찾을 수 없습니다.",
     * "path": "/api/users/123"
     * }
     */

    /**
     * ServiceException 발생 시 호출되는 예외 처리 메서드입니다.
     * 서버 오류는 error 레벨로, 클라이언트 오류는 warn 레벨로 로깅하며,
     * 서버 오류 발생 시 메트릭을 수집합니다.
     *
     * @param e       ServiceException 예외 객체
     * @param request HttpServletRequest 객체
     * @return ErrorResponse를 포함한 ResponseEntity
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException e,
                                                                HttpServletRequest request) {
        String message = messageService.getMessage(e.getErrorCode().getMessageCode());
        HttpStatus status = e.getErrorCode().getStatus();
        String uri = request.getRequestURI();
        String code = e.getErrorCode().getCode();

        if (status.is5xxServerError()) {
            log.error("[SERVER ERROR] - URI: {} | code: {} | message: {}", uri, code, message);

            // 서버 오류 발생 시 메트릭 카운트 증가
            meterRegistry.counter(
                    "server_error_count",
                    "type", "server",
                    "exception", "ServiceException",
                    "method", request.getMethod(),
                    "uri", request.getRequestURI()
            ).increment();
        } else {
            log.warn("[CLIENT ERROR] - URI: {} | code: {} | message: {}", uri, code, message);
        }

        ErrorResponse errorResponse = ErrorResponse.of(e, request, messageService);
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * 데이터베이스 접근 예외를 처리합니다.
     *
     * @param e       DataAccessException 인스턴스
     * @param request HttpServletRequest 인스턴스
     * @return DBErrorResponse 를 포함한 ResponseEntity
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleDataAccessException(DataAccessException e,
                                                   HttpServletRequest request) {

        // 1. 에러 로그 기록 (상세 정보 포함)
        log.error("[DB SERVER ERROR] - URI: {} | message: {}", request.getRequestURI(), e.getMessage(), e);

        // 2. 메트릭 수집 (DB 관련 서버 에러 카운트)
        meterRegistry.counter(
                "server_error_count",              // 메트릭 이름
                "type", "db",                      // 에러 타입 구분 태그
                "exception", "DataAccessException", // 예외 타입 태그
                "method", request.getMethod(),     // HTTP 메서드 태그
                "uri", request.getRequestURI()     // 요청 URI 태그
        ).increment();

        // 3. 사용자에게 반환할 에러 응답 생성
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code("500-0")
                .message("DB 접근 중 오류가 발생했습니다.")
                .path(request.getRequestURI())
                .build();
    }

    /**
     * MethodArgumentNotValidException 처리 메서드입니다.
     * 유효성 검증 실패 시 발생하는 예외를 처리하여, 클라이언트에게 상세한 에러 메시지를 반환합니다.
     *
     * @param ex      MethodArgumentNotValidException 예외 객체
     * @param request HttpServletRequest 객체
     * @return ErrorResponse 유효성 검증 실패 상세 정보
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // 모든 필드 에러의 메시지를 리스트로 수집
        List<String> errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        log.warn("[CLIENT ERROR] 유효성 검사 실패 - URI: {} | 에러 메시지 개수: {} | 메시지 목록: {}",
                request.getRequestURI(),
                errorMessages.size(),
                errorMessages);

        return ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                errorMessages,
                request.getRequestURI()
        );
    }

    /**
     * 예기치 않은 예외를 처리하는 메서드입니다.
     * 서버에서 발생한 모든 예외를 처리하여, 클라이언트에게 기본적인 에러 메시지를 반환합니다.
     *
     * @param e       Exception 예외 객체
     * @param request HttpServletRequest 객체
     * @return ErrorResponse 기본 에러 메시지
     */
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleUnexpectedException(Exception e, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();

        log.error("[UNHANDLED SERVER ERROR] {} {} | ErrorID: {} | Exception: {} | Message: {}",
                request.getMethod(),
                request.getRequestURI(),
                errorId,
                e.getClass().getSimpleName(),
                e.getMessage(),
                e
        );

        // 메트릭 수집
        meterRegistry.counter("unhandled_server_errors",
                "type", "server",
                "exception", e.getClass().getSimpleName(), // 예외 타입 태그
                "method", request.getMethod(),
                "uri", request.getRequestURI()
        ).increment();

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code("500")
                .message("알 수 없는 서버 오류가 발생했습니다.")
                .errors(List.of(e.getClass().getSimpleName() + ": " + e.getMessage()))
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAuthorizationDenied(AuthorizationDeniedException e, HttpServletRequest request) {
        log.warn("[CLIENT ERROR] 접근 거부 - URI: {} | message: {}", request.getRequestURI(), e.getMessage());
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .code("403-1")
                .message("접근이 거부되었습니다.")
                .errors(List.of(e.getClass().getSimpleName() + ": " + e.getMessage()))
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(LockAcquisitionException.class)
    @ResponseStatus(HttpStatus.LOCKED)// 423 error - Locked
    public ErrorResponse handleLockAcquisitionException(LockAcquisitionException e,
                                                        HttpServletRequest request) {
        String message = messageService.getMessage(e.getErrorCode().getMessageCode());
        String uri = request.getRequestURI();
        String code = e.getErrorCode().getCode();

        log.warn("[CLIENT ERROR] LOCK 획득 실패 - URI: {} | code: {} | message: {}", uri, code, message);

        return ErrorResponse.of(e, request, messageService);
    }
}
