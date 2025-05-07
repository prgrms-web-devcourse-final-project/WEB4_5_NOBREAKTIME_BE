package com.mallang.mallang_backend.domain.videohistory.listener;

import com.mallang.mallang_backend.domain.videohistory.event.VideoViewedEvent;
import com.mallang.mallang_backend.domain.videohistory.service.VideoHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 비디오 조회 이벤트를 비동기로 처리
 * - @Async("videoHistoryExecutor"): 별도 스레드풀에서 실행
 * - @EventListener: VideoViewedEvent 발행 시 자동 호출
 *
 * @param event VideoViewedEvent 객체 (memberId, videoId 포함)
 */
@Component
@RequiredArgsConstructor
public class VideoHistoryListener {

	private final VideoHistoryService historyService;

	@Async("videoHistoryExecutor")
	@EventListener
	public void handleVideoViewed(VideoViewedEvent event) {
		// 실제 히스토리 저장 로직 호출
		historyService.save(event.getMemberId(), event.getVideoId());
	}
}