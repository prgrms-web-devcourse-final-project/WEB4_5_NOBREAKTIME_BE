package com.mallang.mallang_backend.domain.videohistory.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mallang.mallang_backend.domain.videohistory.event.VideoViewedEvent;
import com.mallang.mallang_backend.domain.videohistory.service.VideoHistoryService;

class VideoHistoryListenerTest {

	private VideoHistoryService historyService;
	private VideoHistoryListener listener;

	@BeforeEach
	void setUp() {
		historyService = Mockito.mock(VideoHistoryService.class);
		listener = new VideoHistoryListener(historyService);
	}

	@Test
	@DisplayName("VideoViewedEvent 처리 시 historyService.save 호출")
	void handleVideoViewed_invokesSave() {
		// given
		Long memberId = 42L;
		String videoId = "vid-123";
		VideoViewedEvent event = new VideoViewedEvent(memberId, videoId);

		// when
		listener.handleVideoViewed(event);

		// then
		Mockito.verify(historyService)
			.save(memberId, videoId);
	}
}
