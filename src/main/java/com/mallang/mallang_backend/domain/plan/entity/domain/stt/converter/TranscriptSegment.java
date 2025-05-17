package com.mallang.mallang_backend.domain.plan.entity.domain.stt.converter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TranscriptSegment {

	// 자막 ID
	private Long id;

	// 시작 시간 (ex. "00:01:23.500")
	private String startTime;

	// 끝나는 시간 (ex. "00:01:26.200")
	private String endTime;

	// 화자 이름
	private String speaker;

	// 문장 내용
	private String text;
}
