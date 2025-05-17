package com.mallang.mallang_backend.domain.plan.entity.domain.stt.converter;

import java.util.List;

/**
 * 원본 자막 데이터를 포함하는 객체
 */
public class Transcript {
	private List<TranscriptSegment> segments;

	public Transcript(List<TranscriptSegment> segments) {
		this.segments = segments;
	}

	public List<TranscriptSegment> getSegments() {
		return segments;
	}

	/**
	 * Open AI 에서 사용하기 위한 프롬프트 형식으로 원본 자막을 반환합니다.
	 * @return "문장 시작 시간 | 문장 종료 시간 | 화자 | 문장\n ..." 형식의 원본 스크립트
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (TranscriptSegment segment : segments) {
			sb.append(segment.getStartTime())
				.append(" | ")
				.append(segment.getEndTime())
				.append(" | ")
				.append(segment.getSpeaker())
				.append(" | ")
				.append(segment.getText())
				.append("\n");
		}
		return sb.toString();
	}
}
