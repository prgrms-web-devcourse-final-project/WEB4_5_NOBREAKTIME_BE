package com.mallang.mallang_backend.domain.voca.word.listener;

import com.mallang.mallang_backend.domain.voca.word.event.NewWordSearchedEvent;
import com.mallang.mallang_backend.domain.voca.word.service.WordService;
import com.mallang.mallang_backend.global.common.Language;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class WordEventListener {

	private final WordService wordService;

	@Async("addWordExecutor")
	@TransactionalEventListener
	public void handleVideoViewed(NewWordSearchedEvent event) {
		String word = event.getWord();
		Language language = event.getLanguage();
		wordService.savedWord(word, language);
		log.debug("[NewWordSearchedEvent] 단어 저장 완료 {}", word);
	}
}

