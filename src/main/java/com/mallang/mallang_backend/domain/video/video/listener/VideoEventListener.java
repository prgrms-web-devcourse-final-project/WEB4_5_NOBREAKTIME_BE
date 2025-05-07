package com.mallang.mallang_backend.domain.video.video.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.video.video.event.KeywordSavedEvent;
import com.mallang.mallang_backend.domain.voca.word.service.WordService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VideoEventListener {
	private final WordService wordService;

	@Async("videoExecutor")
	@TransactionalEventListener
	public void handleVideoViewed(KeywordSavedEvent event) {
		Keyword keyword = event.getKeyword();
		wordService.savedWord(keyword.getWord());
	}
}
