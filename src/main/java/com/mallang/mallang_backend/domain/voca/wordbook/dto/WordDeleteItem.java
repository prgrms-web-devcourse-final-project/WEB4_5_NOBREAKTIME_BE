package com.mallang.mallang_backend.domain.voca.wordbook.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WordDeleteItem {
	private Long wordbookId;
	private String word;
}