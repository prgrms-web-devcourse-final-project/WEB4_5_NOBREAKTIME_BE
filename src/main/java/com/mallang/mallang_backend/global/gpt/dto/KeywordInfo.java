package com.mallang.mallang_backend.global.gpt.dto;

import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.voca.word.entity.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 키워드(또는 숙어) 정보 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KeywordInfo {
    private String word;
    private String meaning;
    private int difficulity;    // 1 ~ 5

    public Keyword toEntity(Videos video, Subtitle subtitle) {
        return Keyword.builder()
                .videos(video)
                .subtitle(subtitle)
                .word(word)
                .meaning(meaning)
                .difficulty(Difficulty.fromValue(difficulity))
                .build();
    }
}
