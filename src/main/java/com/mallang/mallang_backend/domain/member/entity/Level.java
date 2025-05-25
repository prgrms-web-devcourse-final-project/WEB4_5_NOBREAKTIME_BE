package com.mallang.mallang_backend.domain.member.entity;

import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.Getter;

import static com.mallang.mallang_backend.global.exception.ErrorCode.LEVEL_PARSE_FAILED;

@Getter
public enum Level {
	NONE("-"),
	S("S"),
	A("A"),
	B("B"),
	C("C");

	private String label;

	Level(String label) {
		this.label = label;
	}

	public static Level fromString(String str) {
		for (Level level : Level.values()) {
			if (level.toString().equalsIgnoreCase(str)) {
				return level;
			}
		}
		throw new ServiceException(LEVEL_PARSE_FAILED);
	}
}
