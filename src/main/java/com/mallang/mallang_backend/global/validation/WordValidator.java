package com.mallang.mallang_backend.global.validation;

import com.mallang.mallang_backend.global.common.Language;

import java.util.regex.Pattern;

public class WordValidator {
	private static final Pattern WORD_PATTERN = Pattern.compile("^[a-zA-Z]+$");

	public static boolean isLanguageMatch(String word, Language language) {
		// 각 언어만 포함된 단어인지 검사
		return language.matches(word);
	}
}
