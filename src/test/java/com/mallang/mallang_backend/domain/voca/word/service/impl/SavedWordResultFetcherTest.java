package com.mallang.mallang_backend.domain.voca.word.service.impl;

import com.mallang.mallang_backend.domain.voca.word.entity.Difficulty;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedWordResultFetcherTest {

    @Mock private WordRepository wordRepository;
    @InjectMocks private SavedWordResultFetcher savedWordResultFetcher;

    @Test
    @DisplayName("SavedWordResultFetcher는 DB에서 단어 결과를 조회하여 반환한다")
    void fetchSavedWordResultAfterWait_returnsExpectedWords() {
        Word word = Word.builder()
                .word("light")
                .pos("형용사")
                .meaning("가벼운")
                .difficulty(Difficulty.EASY)
                .exampleSentence("This bag is very light.")
                .translatedSentence("이 가방은 매우 가볍다.")
                .build();

        when(wordRepository.findByWord("light")).thenReturn(List.of(word));

        List<Word> result = savedWordResultFetcher.fetchSavedWordResultAfterWait("light");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMeaning()).isEqualTo("가벼운");
    }
}
