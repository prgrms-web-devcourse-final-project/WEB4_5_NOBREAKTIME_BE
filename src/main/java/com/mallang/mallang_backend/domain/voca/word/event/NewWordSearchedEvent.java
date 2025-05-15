package com.mallang.mallang_backend.domain.voca.word.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NewWordSearchedEvent {
	String word;
}
