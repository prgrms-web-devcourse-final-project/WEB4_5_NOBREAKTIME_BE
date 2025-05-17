package com.mallang.mallang_backend.domain.plan.entity.domain.voca.word.dto;

import com.mallang.mallang_backend.domain.plan.entity.domain.voca.word.entity.Word;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WordSearchResponse {
    private List<WordMeaning> meanings;

    public WordSearchResponse(List<WordMeaning> meanings) {
        this.meanings = meanings;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class WordMeaning {
        private String partOfSpeech;    // 품사
        private String meaning;         // 해석
        private int difficulty;         // 난이도

        /**
         * Entity (Word) 객체를 DTO (WordMeaning) 객체로 변환
         */
        public static WordMeaning fromEntity(Word word) {
            WordMeaning wm = new WordMeaning();
            wm.partOfSpeech = word.getPos();
            wm.meaning = word.getMeaning();
            wm.difficulty = word.getDifficulty().getValue();
            return wm;
        }
    }
}
