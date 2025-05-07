package com.mallang.mallang_backend.global.exception;

import com.mallang.mallang_backend.global.exception.message.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ErrorResponse {

    private final LocalDateTime timestamp; // 발생 시간
    private final int status; // HTTP 상태 코드
    private final String code; // 커스텀 에러 코드
    private final String message; // 클라이언트 표시용 메시지
    private final List<String> errors;
    private final String path; // 요청 경로

    /**
     * 메시지 프로퍼티 변환 -> ErrorResponse 의 팩토리 메서드
     */
    public static ErrorResponse of(
            ServiceException e,
            HttpServletRequest request,
            MessageService messageService
    ) {
        String clientMessage = messageService.getMessage(e.getErrorCode().getMessageCode());
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(e.getErrorCode().getStatus().value()) // HttpStatus에서 int 값 추출
                .code(e.getErrorCode().getCode())
                .message(clientMessage)
                .path(request.getRequestURI())
                .build();
    }

    public static ErrorResponse of(
            int status,
            List<String> errors,
            String path
    ) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .errors(errors)
                .path(path)
                .build();
    }
}
