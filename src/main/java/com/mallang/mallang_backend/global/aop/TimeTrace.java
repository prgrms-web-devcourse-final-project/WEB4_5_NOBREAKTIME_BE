package com.mallang.mallang_backend.global.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 각 메서드의 전체 실행 시간 확인하는 애노테이션
 */
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeTrace {
}
