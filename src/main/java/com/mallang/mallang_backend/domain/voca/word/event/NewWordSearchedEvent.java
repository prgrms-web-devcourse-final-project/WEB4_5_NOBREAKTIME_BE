package com.mallang.mallang_backend.domain.voca.word.event;

import com.mallang.mallang_backend.global.common.Language;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NewWordSearchedEvent {
	String word;
	Language language;
}
