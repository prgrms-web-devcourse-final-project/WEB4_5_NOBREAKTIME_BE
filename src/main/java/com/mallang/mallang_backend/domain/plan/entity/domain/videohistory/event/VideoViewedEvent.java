package com.mallang.mallang_backend.domain.plan.entity.domain.videohistory.event;

import lombok.Getter;

/**
 * 비디오 조회 이벤트
 * - 컨트롤러에서 조회 직후 발행
 * - 리스너에서 히스토리 저장·분석 트리거용
 */
@Getter
public class VideoViewedEvent {

	/** 조회한 회원 ID */
	private final Long memberId;

	/** 조회한 비디오 ID */
	private final String videoId;

	public VideoViewedEvent(Long memberId, String videoId) {
		this.memberId = memberId;
		this.videoId = videoId;
	}
}