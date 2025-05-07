package com.mallang.mallang_backend.global.resilience4j.code;

import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestService {

    private Integer i = 1;

    @CircuitBreaker(name = "oauthUserLoginService", fallbackMethod = "fallbackMethod")
    public void testMethod() {
        log.info("외부 API 호출 실패 - {} 회 시도", i++);
        throw new ServiceException(ErrorCode.API_ERROR);
    }

    @CircuitBreaker(name = "oauthUserLoginService", fallbackMethod = "fallbackMethod")
    public void successMethod() {
        log.info("외부 API 호출 성공");
    }

    private void fallbackMethod(Exception e) {
        if (e instanceof CallNotPermittedException) {
            log.warn("외부 API 호출 중지");
            throw new ServiceException(ErrorCode.API_ERROR, e);
        }
        throw new ServiceException(ErrorCode.API_ERROR, e);
    }
}
