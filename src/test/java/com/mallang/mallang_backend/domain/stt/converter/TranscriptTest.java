package com.mallang.mallang_backend.domain.stt.converter;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class TranscriptTest {

	private final TranscriptParserImpl transcriptParserImpl = new TranscriptParserImpl(new ObjectMapper());

	@Test
	@DisplayName("Transcript 객체로 변환한 Clova Speech 응답을 OpenAI에 사용하기 위한 문자열로 변환할 수 있다")
	void parseActualClovaResponse() throws IOException {
		String json = """
        {
          "result":"COMPLETED",
          "message":"Succeeded",
          "segments":[
            {
              "start":1320,
              "end":11740,
              "textEdited":"Who is it you think you see? Do you know how much I make a year? I mean even if I told you you wouldn't believe it",
              "speaker":{"name":"A"}
            },
            {
              "start":11740,
              "end":21515,
              "textEdited":"Do you know what would happen if I suddenly decided to stop going into work a business big enough that it could be listed on the NASDAQ goes belly up, disappears",
              "speaker":{"name":"A"}
            },
            {
              "start":21515,
              "end":28515,
              "textEdited":"It ceases to exist without me No, you clearly don't know who you're talking to So let me clue you in",
              "speaker":{"name":"A"}
            },
            {
              "start":28515,
              "end":36725,
              "textEdited":"I am not in danger Skyler I am the danger. A guy opens his door and gets shot and you think that of me No,",
              "speaker":{"name":"A"}
            },
            {
              "start":36725,
              "end":39140,
              "textEdited":"I am the one who knocks.",
              "speaker":{"name":"A"}
            }
          ]
        }
        """;

		Transcript transcript = transcriptParserImpl.parseTranscriptJson(json);

		List<TranscriptSegment> segments = transcript.getSegments();
		assertThat(segments).hasSize(5);

		String result = transcript.toString().trim();
		System.out.println(result);

		assertThat(result).startsWith("00:00:01.320 | 00:00:11.740 | A | Who is it you think you see?");
		assertThat(result).contains("00:00:11.740 | 00:00:21.515 | A | Do you know what would happen");
		assertThat(result).contains("00:00:36.725 | 00:00:39.140 | A | I am the one who knocks.");
	}
}