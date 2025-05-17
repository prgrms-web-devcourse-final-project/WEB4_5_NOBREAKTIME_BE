package com.mallang.mallang_backend.domain.voca.wordbook.dto;

import com.mallang.mallang_backend.global.common.Language;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WordbookResponse {
	private Long wordbookId;
	private String name;
	private Language language;
	private int wordCount;
	private int learnedWordCount;
}