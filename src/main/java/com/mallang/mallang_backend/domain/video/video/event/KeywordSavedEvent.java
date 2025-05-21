package com.mallang.mallang_backend.domain.video.video.event;

import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.global.common.Language;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KeywordSavedEvent {
	private Keyword keyword;
	private Language language;
}
