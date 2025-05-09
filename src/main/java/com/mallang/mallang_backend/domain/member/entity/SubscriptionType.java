package com.mallang.mallang_backend.domain.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 사용자 권한 설정을 위해 enum 타입에 권한 이름을 동시에 부여
 *
 * enum 이 보장하는 역할명을 바로 사용 ->
 * 역할명이 바뀌거나 구독 등급이 추가될 때 enum 만 수정
 * 시스템 설계상 role 값이 null 이 될 수 없으므로, 예외 처리 없이 바로 반환 가능
 */
@Getter
@AllArgsConstructor
public enum SubscriptionType {
    NONE("DELETE_USER", 0),
    BASIC("ROLE_BASIC", 0),
    STANDARD("ROLE_STANDARD", 4500),
    PREMIUM("ROLE_PREMIUM", 8500),
    ADMIN("ROLE_ADMIN", 0); // 추후 관리자 권한 사용 가능성으로 추가

    private final String roleName;
    private final int basePrice; // 1개월 기준 금액
}