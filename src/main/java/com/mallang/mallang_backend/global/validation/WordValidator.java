package com.mallang.mallang_backend.global.validation;

import java.util.regex.Pattern;

import com.mallang.mallang_backend.global.common.Language;

public class WordValidator {

	public static boolean isLanguageMatch(String word, Language language) {
		// 각 언어만 포함된 단어인지 검사
		return language.matches(word);
	}
}
