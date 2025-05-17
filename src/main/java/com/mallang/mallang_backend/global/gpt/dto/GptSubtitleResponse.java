package com.mallang.mallang_backend.global.gpt.dto;

import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


/**
 * GPT 분석 결과 + 자막 정보 + 키워드 리스트를 포함하는 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GptSubtitleResponse {
    private Long subtitleId;            // 자막 아이디
    private String startTime;           // 자막 시작 시간
    private String endTime;             // 자막 종료 시간
    private String speaker;             // 화자 정보
    private String original;            // 원문 문장
    private String transcript;          // 번역 문장
    private List<KeywordInfo> keywords; // 키워드 리스트

    public static List<GptSubtitleResponse> from(List<Subtitle> subtitles) {
        return subtitles.stream()
                .map(s -> new GptSubtitleResponse(
                        s.getId(),
                        s.getStartTime(),
                        s.getEndTime(),
                        s.getSpeaker(),
                        s.getOriginalSentence(),
                        s.getTranslatedSentence(),
                        s.getKeywords().stream()
                                .map(k -> new KeywordInfo(
                                        k.getWord(),
                                        k.getMeaning(),
                                        k.getDifficulty().getValue()
                                ))
                                .toList()
                ))
                .toList();
    }

}
