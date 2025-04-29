package com.mallang.mallang_backend.domain.stt.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TranscriptParser {
	/**
	 *
	 * @param json Clova Speech의 응답 Json String
	 * @return Transcript 객체
	 * @throws IOException Json 파싱 실패
	 */
	public static Transcript parseTranscriptJson(String json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(json);
		JsonNode segmentsNode = root.get("segments");

		List<TranscriptSegment> result = new ArrayList<>();
		for (JsonNode segment : segmentsNode) {
			long startMs = segment.get("start").asLong();
			long endMs = segment.get("end").asLong();
			String speaker = segment.path("speaker").path("name").asText("Unknown");
			String text = segment.get("textEdited").asText();

			String startTime = formatMillis(startMs);
			String endTime = formatMillis(endMs);

			result.add(new TranscriptSegment(startTime, endTime, speaker, text));
		}

		return new Transcript(result);
	}

	/**
	 * Clova Speech가 응답한 세그먼트 시간(ms)을 00:00:00.000 형식 String으로 변환합니다.
	 * @param millis ms 단위 세그먼트 시간
	 * @return 00:00:00.000 형식 String
	 */
	private static String formatMillis(long millis) {
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
		long ms = millis % 1000;
		return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, ms);
	}
}
