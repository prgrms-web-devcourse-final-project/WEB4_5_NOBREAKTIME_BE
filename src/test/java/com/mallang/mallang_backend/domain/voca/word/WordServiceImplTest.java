package com.mallang.mallang_backend.domain.voca.word;

import com.mallang.mallang_backend.domain.voca.word.dto.WordSearchResponse;
import com.mallang.mallang_backend.domain.voca.word.entity.Difficulty;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.word.service.impl.WordServiceImpl;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import com.mallang.mallang_backend.global.util.redis.RedisDistributedLock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.mallang.mallang_backend.global.common.Language.ENGLISH;
import static com.mallang.mallang_backend.global.gpt.util.GptScriptProcessor.parseGptResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WordServiceImplTest {
    @Mock
    private WordRepository wordRepository;

    @Mock
    private GptService gptService;

    @Mock
    private RedisDistributedLock redisDistributedLock;

    @InjectMocks
    private WordServiceImpl wordService;

    @Test
    @DisplayName("DB에 단어가 있으면 GPT 호출 없이 결과 반환")
    void savedWord_foundInDb() {
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
        WordSearchResponse response = wordService.savedWord("light", ENGLISH);

        // then
        assertThat(response.getMeanings()).hasSize(1);
        verify(gptService, never()).searchWord("light", ENGLISH);
    }

    @Test
    @DisplayName("DB에 없으면 GPT 결과 파싱 후 저장하고 반환")
    void savedWord_generatedFromGpt() {
        // given
        String gptResult = "형용사 | 가벼운 | 1 | This bag is very light. | 이 가방은 매우 가볍다.";
        when(wordRepository.findByWord("light")).thenReturn(List.of());
        when(gptService.searchWord("light", ENGLISH)).thenReturn(parseGptResult("light", gptResult));
        when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(true);
        // when
        WordSearchResponse response = wordService.savedWord("light", ENGLISH);

        // then
        assertThat(response.getMeanings()).hasSize(1);
        verify(wordRepository).saveAll(anyList());
    }
}
