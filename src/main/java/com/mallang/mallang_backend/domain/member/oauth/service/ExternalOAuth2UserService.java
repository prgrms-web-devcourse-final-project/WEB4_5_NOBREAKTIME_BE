package com.mallang.mallang_backend.domain.member.oauth.service;

import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

/**
 * 외부 API 호출 전용 서비스(트랜잭션 미적용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalOAuth2UserService {
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Retry(name = "oauthUserLoginService")
    @CircuitBreaker(name = "oauthUserLoginService", fallbackMethod = "circuitBreakerFallbackMethod")
    public OAuth2User loadUser(OAuth2UserRequest request) {
        return delegate.loadUser(request);
    }


    public OAuth2User circuitBreakerFallbackMethod(OAuth2UserRequest userRequest,
                                                   Throwable t
    ) {
        handleErrorLogs(t);
        throw new ServiceException(API_BLOCK);
    }

    private void handleErrorLogs(Throwable throwable
    ) {
        if (throwable instanceof ConnectException) {
            log.error("[서킷 브레이커] OAuth2 로그인 실패 - 소셜 서버 연결 불가", throwable.getCause());
        } else if (throwable instanceof SocketTimeoutException) {
            log.error("[서킷 브레이커] OAuth2 로그인 실패 - 소셜 서버 응답 시간 초과", throwable.getCause());
        } else if (throwable instanceof SSLHandshakeException) {
            log.error("[서킷 브레이커] OAuth2 로그인 실패 - SSL 인증서 오류", throwable.getCause());
        } else if (throwable instanceof HttpServerErrorException) {
            log.error("[서킷 브레이커] OAuth2 로그인 실패 - 소셜 서버 내부 오류(5xx)", throwable.getCause());
        } else if (throwable instanceof ResourceAccessException) {
            log.error("[서킷 브레이커] OAuth2 로그인 실패 - 리소스 접근 실패", throwable.getCause());
        } else if (throwable instanceof HttpClientErrorException.TooManyRequests) {
            log.error("[서킷 브레이커] OAuth2 로그인 실패 - 요청 횟수 초과(429)", throwable.getCause());
        } else if (throwable instanceof TransientDataAccessException) {
            log.error("[서킷 브레이커] OAuth2 로그인 실패 - 일시적 DB 오류", throwable.getCause());
        } else {
            log.error("[서킷 브레이커] OAuth2 로그인 실패 - 알 수 없는 오류", throwable.getCause());
        }
    }

    private void handleTooManyRequests(HttpClientErrorException e) {
        HttpHeaders headers = e.getResponseHeaders();
        String retryAfter = headers != null ? headers.getFirst("Retry-After") : "60";
        log.error("API 호출 제한 - 재시도까지 {}초 남음", retryAfter);
        throw new ServiceException(ErrorCode.OAUTH_RATE_LIMIT);
    }

    private void handleOAuthException(OAuth2AuthenticationException ex) {
        OAuth2Error error = ex.getError();
        if (error.getErrorCode().equals("invalid_token")) {
            throw new ServiceException(INVALID_TOKEN, ex);
        }
        throw new ServiceException(UNSUPPORTED_OAUTH_PROVIDER, ex);
    }
}