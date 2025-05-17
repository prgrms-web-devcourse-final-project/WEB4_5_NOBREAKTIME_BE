package com.mallang.mallang_backend.domain.plan.entity.domain.video.youtube.mapper;

import java.time.Duration;

public class DurationMapper {
	// SO 8601 포맷(PT19M32S)을 java.time.Duration 객체로 변환
	public static Duration parseDuration(String isoDuration) {
		try {
			return Duration.parse(isoDuration);
		} catch (Exception e) {
			return null;
		}
	}
}
