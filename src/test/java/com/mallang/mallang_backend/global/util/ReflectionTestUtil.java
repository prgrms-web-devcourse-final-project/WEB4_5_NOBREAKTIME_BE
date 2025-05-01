package com.mallang.mallang_backend.global.util;

import java.lang.reflect.Field;

/**
 * JPA 로 관리되는 ID 값을 테스트 용도로 초기화하기 위한 테스트 유틸 클래스
 */
public class ReflectionTestUtil {

	/**
	 * 엔티티의 id 필드 또는 ~Id 필드를 찾아 리플렉션으로 값 설정
	 *
	 * @param entity 대상 객체
	 * @param id     설정할 ID 값
	 */
	public static void setId(Object entity, Long id) {
		try {
			Field idField = findIdField(entity.getClass());
			idField.setAccessible(true);
			idField.set(entity, id);
		} catch (Exception e) {
			throw new RuntimeException("ID 설정 실패: " + e.getMessage(), e);
		}
	}

	private static Field findIdField(Class<?> clazz) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField("id");
		} catch (NoSuchFieldException e) {
			// 예: wordbookId, memberId 등
			for (Field field : clazz.getDeclaredFields()) {
				if (field.getName().toLowerCase().endsWith("id")) {
					return field;
				}
			}
			throw new NoSuchFieldException("id 또는 ~Id 필드를 찾을 수 없습니다.");
		}
	}
}
