package com.mallang.mallang_backend.global.exception;

import com.mallang.mallang_backend.global.exception.message.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageService messageService;

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

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException e,
                                                                HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(e, request, messageService);
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(errorResponse);
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

        // ErrorResponse에 여러 메시지가 담길 수 있도록 생성자/팩토리 메서드 수정 필요
        return ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                errorMessages,
                request.getRequestURI()
        );
    }
}
