package com.mallang.mallang_backend.domain.video.video.listener;

import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.video.video.event.KeywordSavedEvent;
import com.mallang.mallang_backend.domain.video.video.event.VideoAnalyzedEvent;
import com.mallang.mallang_backend.domain.voca.word.service.WordService;
import com.mallang.mallang_backend.global.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoEventListener {
	private final WordService wordService;

	@Async("addWordExecutor")
	@TransactionalEventListener
	public void handleVideoViewed(KeywordSavedEvent event) {
		Keyword keyword = event.getKeyword();
		wordService.savedWord(keyword.getWord(), event.getLanguage());
		log.debug("[KeywordSavedEvent] 단어 저장 완료 {}", keyword.getWord());
	}

	@Async("audioDeleteExecutor")
	@TransactionalEventListener
	public void handleVideoAnalyzed(VideoAnalyzedEvent event) {
		String fileName = event.getFileName();
		String path = AppConstants.UPLOADS_DIR + fileName;

		try {
			Path filePath = Paths.get(path);
			Files.deleteIfExists(filePath);
			log.debug("Deleted file: {}", path);
		} catch (IOException e) {
			log.warn("Failed to delete file: {}", path, e);
		}
	}
}
