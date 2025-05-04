package com.mallang.mallang_backend.global.exception;

import com.mallang.mallang_backend.global.exception.message.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("ServiceException 발생 시 ErrorResponse에 code, message, path가 올바르게 설정된다")
    void handleServiceException_returnsFormattedErrorResponse() {
        // given
        ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;
        ServiceException serviceException = new ServiceException(errorCode);

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/api/users/123");

        MessageService mockMessageService = mock(MessageService.class);
        when(mockMessageService.getMessage(errorCode.getMessageCode()))
                .thenReturn("사용자를 찾을 수 없습니다.");

        GlobalExceptionHandler handler = new GlobalExceptionHandler(mockMessageService);

        // when
        ResponseEntity<ErrorResponse> response = handler.handleServiceException(serviceException, mockRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo(errorCode.getCode());
        assertThat(body.getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
        assertThat(body.getPath()).isEqualTo("/api/users/123");
        assertThat(body.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
