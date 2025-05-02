package com.mallang.mallang_backend.global.gpt.util;

import com.mallang.mallang_backend.domain.stt.converter.TranscriptSegment;
import com.mallang.mallang_backend.global.gpt.dto.GptSubtitleResponse;
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
                new TranscriptSegment("00:00:01.320","00:00:11.740","A", "Who is it you think you see? Do you know how much I make a year? I mean even if I told you you wouldn't believe it"),
                new TranscriptSegment("00:00:11.740","00:00:21.515","A", "Do you know what would happen if I suddenly decided to stop going into work a business big enough that it could be listed on the NASDAQ goes belly up, disappears"),
                new TranscriptSegment("00:00:21.515","00:00:28.515","A", "It ceases to exist without me No, you clearly don't know who you're talking to So let me clue you in"),
                new TranscriptSegment("00:00:28.515","00:00:36.725","A", "I am not in danger Skyler I am the danger. A guy opens his door and gets shot and you think that of me No,"),
                new TranscriptSegment("00:00:36.725","00:00:39.140","A", "I am the one who knocks.")
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

    @Test
    @DisplayName("GPT 응답 문자열을 파싱하여 GptSubtitleResult 리스트로 변환")
    void parseAnalysisResultTest() {
        String gptResponse = """
                Who is it you think you see? | 네가 보고 있다고 생각하는 사람이 누구지? | see | 보다 | 1 | believe | 믿다 | 2 | make a year | 1년에 얼마를 벌다 | 3
                ---
                I am the one who knocks. | 문을 두드리는 사람은 나야 | knock | 두드리다 | 1
                ---
                """;

        List<TranscriptSegment> segments = List.of(
                new TranscriptSegment("00:00:01.000", "00:00:05.000", "A", "Who is it you think you see?"),
                new TranscriptSegment("00:00:06.000", "00:00:08.000", "A", "I am the one who knocks.")
        );

        List<GptSubtitleResponse> result = GptScriptProcessor.parseAnalysisResult(gptResponse, segments);

        assertThat(result).hasSize(2);

        GptSubtitleResponse first = result.get(0);
        assertThat(first.getOriginal()).isEqualTo("Who is it you think you see?");
        assertThat(first.getTranscript()).isEqualTo("네가 보고 있다고 생각하는 사람이 누구지?");
        assertThat(first.getKeywords()).hasSize(3);

        assertThat(first.getKeywords().get(0).getWord()).isEqualTo("see");
        assertThat(first.getKeywords().get(0).getMeaning()).isEqualTo("보다");
        assertThat(first.getKeywords().get(0).getDifficulity()).isEqualTo(1);

        assertThat(first.getKeywords().get(1).getWord()).isEqualTo("believe");
        assertThat(first.getKeywords().get(1).getMeaning()).isEqualTo("믿다");
        assertThat(first.getKeywords().get(1).getDifficulity()).isEqualTo(2);

        assertThat(first.getKeywords().get(2).getWord()).isEqualTo("make a year");
        assertThat(first.getKeywords().get(2).getMeaning()).isEqualTo("1년에 얼마를 벌다");
        assertThat(first.getKeywords().get(2).getDifficulity()).isEqualTo(3);


        GptSubtitleResponse second = result.get(1);
        assertThat(second.getOriginal()).isEqualTo("I am the one who knocks.");
        assertThat(second.getTranscript()).isEqualTo("문을 두드리는 사람은 나야");
        assertThat(second.getKeywords()).hasSize(1);
        assertThat(second.getKeywords().get(0).getWord()).isEqualTo("knock");
        assertThat(second.getKeywords().get(0).getMeaning()).isEqualTo("두드리다");
        assertThat(second.getKeywords().get(0).getDifficulity()).isEqualTo(1);
    }
}
