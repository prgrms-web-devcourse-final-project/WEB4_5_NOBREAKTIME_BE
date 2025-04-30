package com.mallang.mallang_backend.domain.voca.word;

import com.mallang.mallang_backend.domain.voca.word.dto.WordSearchResponse;
import com.mallang.mallang_backend.domain.voca.word.entity.Difficulty;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.word.service.impl.WordServiceImpl;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class WordServiceImplTest {

    private final WordRepository wordRepository = mock(WordRepository.class);
    private final GptService gptService = mock(GptService.class);
    private final WordServiceImpl wordService = new WordServiceImpl(wordRepository, gptService);

    @Test
    @DisplayName("DB에 단어가 있으면 GPT 호출 없이 결과 반환")
    void searchWord_foundInDb() {
        //given
        Word word = Word.builder()
                .word("light")
                .pos("형용사")
                .meaning("가벼운")
                .difficulty(Difficulty.EASY)
                .exampleSentence("This bag is very light.")
                .translatedSentence("이 가방은 매우 가볍다.")
                .build();

        when(wordRepository.findByWord("light")).thenReturn(List.of(word));

        // when
        WordSearchResponse response = wordService.searchWord("light");

        // then
        assertThat(response.getMeanings()).hasSize(1);
        verify(gptService, never()).searchWord("light");
    }

    @Test
    @DisplayName("DB에 없으면 GPT 결과 파싱 후 저장하고 반환")
    void searchWord_generatedFromGpt() {
        // given
        String gptResult = "형용사 | 가벼운 | 1 | This bag is very light. | 이 가방은 매우 가볍다.";
        when(wordRepository.findByWord("light")).thenReturn(List.of());
        when(gptService.searchWord("light")).thenReturn(gptResult);

        // when
        WordSearchResponse response = wordService.searchWord("light");

        // then
        assertThat(response.getMeanings()).hasSize(1);
        verify(wordRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("GPT 결과 형식이 잘못되면 파싱 예외 발생")
    void searchWord_invalidGptFormat() {
        // given
        String invalidGptResult = "형용사 | 가벼운 | This bag is very light."; // 잘못된 포맷
        when(wordRepository.findByWord("light")).thenReturn(List.of());
        when(gptService.searchWord("light")).thenReturn(invalidGptResult);

        // when & then
        assertThatThrownBy(() -> wordService.searchWord("light"))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORD_PARSE_FAILED);

    }

    @Test
    @DisplayName("GPT 파싱 후 저장 시 에러가 발생하면 예외 발생")
    void searchWord_saveFail() {
        // given
        String gptResult = "형용사 | 가벼운 | 1 | This bag is very light. | 이 가방은 매우 가볍다.";
        when(wordRepository.findByWord("light")).thenReturn(List.of());
        when(gptService.searchWord("light")).thenReturn(gptResult);
        doThrow(new RuntimeException("DB Error")).when(wordRepository).saveAll(anyList());

        // when & then
        assertThatThrownBy(() -> wordService.searchWord("light"))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORD_SAVE_FAILED);
    }
}
