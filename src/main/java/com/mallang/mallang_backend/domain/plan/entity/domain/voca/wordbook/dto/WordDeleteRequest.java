package com.mallang.mallang_backend.domain.plan.entity.domain.voca.wordbook.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WordDeleteRequest {
	private List<WordDeleteItem> words;
}