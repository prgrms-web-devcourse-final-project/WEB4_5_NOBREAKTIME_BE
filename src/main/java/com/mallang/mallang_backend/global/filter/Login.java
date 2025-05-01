package com.mallang.mallang_backend.global.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러의 메서드 매개변수로 사용 가능
 * 예시: (@Login CustomUserDetails userDetail)
 *
 * userDetail 에서는 ID, 권한 얻을 수 있음
 * 예시: userDetail.getId / userDetail.getAuthorotie
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Login {
}
