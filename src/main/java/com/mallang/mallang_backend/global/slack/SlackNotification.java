package com.mallang.mallang_backend.global.slack;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlackNotification {
    String title() default "[작업 시작]"; // 기본 제목
    String message() default "작업 시작 알림";  // 기본 메시지
}
