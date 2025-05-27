package com.mallang.mallang_backend.global.gpt.util;

import com.mallang.mallang_backend.domain.stt.converter.TranscriptSegment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GptScriptProcessorTest {

    @Test
    @DisplayName("TranscriptSegment 리스트를 | 구분자로 연결한 문자열로 반환한다.")
        // Todo 메서드명 변경
    void prepareScriptInputTextTest() {
        List<TranscriptSegment> segments = List.of(
                new TranscriptSegment(1L, "00:00:01.320","00:00:11.740","A", "Who is it you think you see? Do you know how much I make a year? I mean even if I told you you wouldn't believe it"),
                new TranscriptSegment(2L, "00:00:11.740","00:00:21.515","A", "Do you know what would happen if I suddenly decided to stop going into work a business big enough that it could be listed on the NASDAQ goes belly up, disappears"),
                new TranscriptSegment(3L, "00:00:21.515","00:00:28.515","A", "It ceases to exist without me No, you clearly don't know who you're talking to So let me clue you in"),
                new TranscriptSegment(4L, "00:00:28.515","00:00:36.725","A", "I am not in danger Skyler I am the danger. A guy opens his door and gets shot and you think that of me No,"),
                new TranscriptSegment(5L, "00:00:36.725","00:00:39.140","A", "I am the one who knocks.")
        );

        String result = GptScriptProcessor.prepareScriptInputText(segments);

        assertThat(result).isEqualTo("Who is it you think you see? Do you know how much I make a year? I mean even if I told you you wouldn't believe it | " +
                "Do you know what would happen if I suddenly decided to stop going into work a business big enough that it could be listed on the NASDAQ goes belly up, disappears | " +
                "It ceases to exist without me No, you clearly don't know who you're talking to So let me clue you in | " +
                "I am not in danger Skyler I am the danger. A guy opens his door and gets shot and you think that of me No, | " +
                "I am the one who knocks.");
    }

    @Test
    @DisplayName("빈 리스트가 주어졌을 때, 빈 문자열 반환")
    void prepareScriptInputTextTest_emptyList() {
        List<TranscriptSegment> emptySegments = List.of();

        String result = GptScriptProcessor.prepareScriptInputText(emptySegments);

        assertThat(result).isEqualTo("");
    }
}
