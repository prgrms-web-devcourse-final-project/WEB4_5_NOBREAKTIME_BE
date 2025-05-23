package com.mallang.mallang_backend.global.gpt.service.impl;

import com.mallang.mallang_backend.domain.stt.converter.TranscriptSegment;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.dto.GptSubtitleResponse;
import com.mallang.mallang_backend.global.gpt.dto.Message;
import com.mallang.mallang_backend.global.gpt.dto.OpenAiResponse;
import com.mallang.mallang_backend.global.gpt.service.GptPromptBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.mallang.mallang_backend.global.common.Language.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GptServiceImplTest {
	@Spy
	@InjectMocks
	private GptServiceImpl gptServiceImpl;

	@Mock
	private GptPromptBuilder gptPromptBuilder;

	@Test
	@DisplayName("스크립트 분석 시 Original에 사용된 단어 그대로의 형태가 아니면 OpenAI 응답의 Keyword에서 제거되어야 한다")
	void analyzeScript_shouldReturnValidKeywordResponses() {
		List<TranscriptSegment> segments = List.of(
			new TranscriptSegment(1L, "00:00:01.000", "00:00:03.000", "A", "It ceases to exist without me")
		);

		String script = "It ceases to exist without me";
		String prompt = "some prompt";

		when(gptPromptBuilder.buildPromptForAnalyzeScript(script)).thenReturn(prompt);

		OpenAiResponse mockResponse = new OpenAiResponse();
		mockResponse.setChoices(List.of(
			new OpenAiResponse.Choice(new Message("user", "It ceases to exist without me | 나 없이는 존재하지 않아 | cease | 멈추다 | 2 | exist | 존재하다 | 2"))
		));

		doReturn(mockResponse).when(gptServiceImpl).callGptApi(prompt);

		// 스크립트 분석
		List<GptSubtitleResponse> result = gptServiceImpl.analyzeScript(segments, ENGLISH);

		assertThat(result).isNotEmpty();
		assertThat(result.get(0).getKeywords()).extracting("word").doesNotContain("cease");
	}

	@Test
	@DisplayName("단어 검색 시 예문에 단어 형태가 일치하지 않으면 예외 발생")
	void searchWord_shouldThrowExceptionWhenExampleSentenceIsInvalid() {
		String word = "cease";
		String prompt = "some prompt";

		when(gptPromptBuilder.buildPromptForSearchWord(word)).thenReturn(prompt);

		OpenAiResponse mockResponse = new OpenAiResponse();
		mockResponse.setChoices(List.of(
			new OpenAiResponse.Choice(new Message("user", "동사 | 멈추다 | 2 | It ceases to exist without me | 나 없이는 존재할 수 없다."))
		));

		doReturn(mockResponse).when(gptServiceImpl).callGptApi(prompt);

		// 예문에 "cease"가 정확히 포함되지 않은 경우 예외 발생
		assertThatThrownBy(() -> {
			gptServiceImpl.searchWord(word, ENGLISH);
		}).isInstanceOf(ServiceException.class);
	}

	@Test
	@DisplayName("단어 검색 시 예문에 단어 형태가 정확히 일치하면 결과 반환")
	void searchWord_shouldReturnValidWords() {
		String word = "ceases";
		String prompt = "some prompt";

		when(gptPromptBuilder.buildPromptForSearchWord(word)).thenReturn(prompt);

		OpenAiResponse mockResponse = new OpenAiResponse();
		mockResponse.setChoices(List.of(
			new OpenAiResponse.Choice(new Message("user", "동사 | 멈추다 | 2 | It ceases to exist without me | 나 없이는 존재할 수 없다."))
		));

		doReturn(mockResponse).when(gptServiceImpl).callGptApi(prompt);

		List<Word> result = gptServiceImpl.searchWord(word, ENGLISH);

		assertThat(result).isNotEmpty();
		assertThat(result.get(0).getWord()).isEqualTo("ceases");
	}
}
