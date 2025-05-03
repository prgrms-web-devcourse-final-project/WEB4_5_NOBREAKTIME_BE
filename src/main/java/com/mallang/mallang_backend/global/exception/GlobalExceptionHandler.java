package com.mallang.mallang_backend.global.exception;

import com.mallang.mallang_backend.global.exception.message.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageService messageService;

    /**
     * 예시 응답
     * {
     *   "timestamp": "2025-04-26T23:20:00",
     *   "status": 404,
     *   "code": "404-1",
     *   "message": "사용자를 찾을 수 없습니다.",
     *   "path": "/api/users/123"
     * }
     */

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException e,
                                                                HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(e, request, messageService);
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(errorResponse);
    }
}
