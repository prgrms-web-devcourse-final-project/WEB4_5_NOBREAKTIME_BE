package com.mallang.mallang_backend.global.swagger;

import com.mallang.mallang_backend.global.exception.ErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PossibleErrors {
    ErrorCode[] value() default {};
}