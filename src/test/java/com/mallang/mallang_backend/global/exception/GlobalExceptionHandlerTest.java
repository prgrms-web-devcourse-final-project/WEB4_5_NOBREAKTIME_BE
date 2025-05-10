package com.mallang.mallang_backend.global.exception;

import com.mallang.mallang_backend.global.exception.message.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("ServiceException 발생 시 ErrorResponse에 code, message, path가 올바르게 설정된다")
    void handleServiceException_returnsFormattedErrorResponse() {
        // given
        ErrorCode errorCode = ErrorCode.MEMBER_NOT_FOUND;
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

    @Test
    @DisplayName("예상치 못한 Exception 발생 시 500 응답과 기본 메시지를 반환한다")
    void handleUnexpectedException_returnsGenericErrorResponse() {
        // given
        Exception e = new NullPointerException("str is null");
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/api/test/crash");

        GlobalExceptionHandler handler = new GlobalExceptionHandler(null); // messageService는 사용 안하므로 null OK

        // when
        ErrorResponse response = handler.handleUnexpectedException(e, mockRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getCode()).isEqualTo("500-0");
        assertThat(response.getMessage()).isEqualTo("알 수 없는 서버 오류가 발생했습니다.");
        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors().get(0)).contains("NullPointerException");
        assertThat(response.getPath()).isEqualTo("/api/test/crash");
    }

    @Test
    @DisplayName("AuthorizationDeniedException 발생 시 403 응답과 메시지를 반환한다")
    void handleAuthorizationDenied_returnsForbiddenErrorResponse() {
        // given
        AuthorizationDeniedException e = new AuthorizationDeniedException("Access Denied");
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/api/protected/resource");

        GlobalExceptionHandler handler = new GlobalExceptionHandler(null); // messageService 사용 안함

        // when
        ErrorResponse response = handler.handleAuthorizationDenied(e, mockRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getCode()).isEqualTo("403-1");
        assertThat(response.getMessage()).isEqualTo("접근이 거부되었습니다.");
        assertThat(response.getErrors()).contains("AuthorizationDeniedException: Access Denied");
        assertThat(response.getPath()).isEqualTo("/api/protected/resource");
    }

}
