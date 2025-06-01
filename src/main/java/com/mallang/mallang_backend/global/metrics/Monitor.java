package com.mallang.mallang_backend.global.metrics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Monitor {
    String name() default ""; // 메트릭 수집 이름
    String[] tags() default {}; // 추가 태그
}
