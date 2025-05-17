package com.mallang.mallang_backend.domain.plan.entity.domain.video.video.event;

import com.mallang.mallang_backend.domain.plan.entity.domain.keyword.entity.Keyword;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KeywordSavedEvent {
	private Keyword keyword;
}
