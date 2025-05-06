package com.mallang.mallang_backend.domain.member.entity;

import lombok.Getter;

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
}
