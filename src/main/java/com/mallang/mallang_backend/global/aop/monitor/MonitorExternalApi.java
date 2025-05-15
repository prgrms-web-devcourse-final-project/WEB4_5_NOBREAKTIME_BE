package com.mallang.mallang_backend.global.aop.monitor;

import com.mallang.mallang_backend.global.dto.TokenUsageType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MonitorExternalApi {
    String name(); // ex: "openai", "youtube"
    TokenUsageType usageType() default TokenUsageType.REQUEST;
}
