package com.mallang.mallang_backend.global.filter.transactionfilter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class MDCFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId); // 또는 "traceId"

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientIp = getClientIp(httpRequest); // 클라이언트 IP 가져오기 (X-Forwarded-For 헤더도 체크)
        MDC.put("clientIp", clientIp);

        String requestUri = httpRequest.getRequestURI();
        MDC.put("requestUri", requestUri); // 요청 URI 가져오기
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("transactionId");
            MDC.remove("clientIp");
            MDC.remove("requestUri");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0]; // 프록시 환경 고려
        }
        return request.getRemoteAddr();
    }
}