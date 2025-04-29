package com.mallang.mallang_backend.domain.stt.converter;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class TranscriptParserImplTest {

	private final TranscriptParserImpl transcriptParserImpl = new TranscriptParserImpl(new ObjectMapper());

	@Test
	@DisplayName("Clova Speech의 실제 응답을 Transcript 형식으로 변환할 수 있다")
	void parseTranscriptJson_withRealJson() throws IOException {
		String json = """
        {
          "result":"COMPLETED",
          "message":"Succeeded",
          "segments":[
            {
              "start":1320,
              "end":11740,
              "speaker":{"name":"A"},
              "textEdited":"Who is it you think you see? Do you know how much I make a year? I mean even if I told you you wouldn't believe it"
            },
            {
              "start":11740,
              "end":21515,
              "speaker":{"name":"A"},
              "textEdited":"Do you know what would happen if I suddenly decided to stop going into work a business big enough that it could be listed on the NASDAQ goes belly up, disappears"
            },
            {
              "start":21515,
              "end":28515,
              "speaker":{"name":"A"},
              "textEdited":"It ceases to exist without me No, you clearly don't know who you're talking to So let me clue you in"
            },
            {
              "start":28515,
              "end":36725,
              "speaker":{"name":"A"},
              "textEdited":"I am not in danger Skyler I am the danger. A guy opens his door and gets shot and you think that of me No,"
            },
            {
              "start":36725,
              "end":39140,
              "speaker":{"name":"A"},
              "textEdited":"I am the one who knocks."
            }
          ]
        }
        """;

		Transcript transcript = transcriptParserImpl.parseTranscriptJson(json);

		List<TranscriptSegment> segments = transcript.getSegments();
		assertThat(segments).hasSize(5);

		TranscriptSegment first = segments.get(0);
		assertThat(first.getStartTime()).isEqualTo("00:00:01.320");
		assertThat(first.getEndTime()).isEqualTo("00:00:11.740");
		assertThat(first.getSpeaker()).isEqualTo("A");
		assertThat(first.getText()).startsWith("Who is it you think you see");

		TranscriptSegment last = segments.get(4);
		assertThat(last.getStartTime()).isEqualTo("00:00:36.725");
		assertThat(last.getEndTime()).isEqualTo("00:00:39.140");
		assertThat(last.getText()).isEqualTo("I am the one who knocks.");
	}
}