package com.mallang.mallang_backend.domain.voca.wordbook.service.impl;

import static com.mallang.mallang_backend.global.util.ReflectionTestUtil.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordToWordbookListRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordToWordbookRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.mallang.mallang_backend.domain.voca.wordbookitem.repository.WordbookItemRepository;

@ExtendWith(MockitoExtension.class)
class WordbookServiceImplTest {

	@InjectMocks
	private WordbookServiceImpl wordbookService;

	@Mock
	private WordbookRepository wordbookRepository;

	@Mock
	private WordRepository wordRepository;

	@Mock
	private WordbookItemRepository wordbookItemRepository;

	private Member savedMember;
	private Wordbook savedWordbook;
	private Word savedWord;

	@BeforeEach
	void setUp() {
		// Member
		savedMember = Member.builder().build();
		setId(savedMember, 1L);

		// Wordbook
		savedWordbook = Wordbook.builder().member(savedMember).build();
		setId(savedWordbook, 100L);

		// Word
		savedWord = Word.builder().word("apple").build();
		setId(savedWord, 200L);
	}

	@Test
	@DisplayName("단어장에 단어를 추가할 수 있다")
	void addWords() {
		AddWordToWordbookRequest dto = new AddWordToWordbookRequest();
		dto.setWord("apple");
		dto.setVideoId(10L);
		dto.setSubtitleId(20L);

		AddWordToWordbookListRequest request = new AddWordToWordbookListRequest();
		request.setWords(List.of(dto));

		given(wordbookRepository.findByIdAndMember(savedWordbook.getId(), savedMember)).willReturn(Optional.of(savedWordbook));
		given(wordRepository.findByWord("apple")).willReturn(List.of(savedWord));
		given(wordbookItemRepository.findByWordbookIdAndWord(savedWordbook.getId(), "apple")).willReturn(Optional.empty());

		wordbookService.addWords(savedWordbook.getId(), request, savedMember);

		then(wordbookItemRepository).should().save(any(WordbookItem.class));
	}

	@Test
	@DisplayName("단어장에 단어를 추가할 수 있다")
	void addWordCustom() {
		AddWordRequest dto = new AddWordRequest();
		dto.setWord("apple");

		given(wordbookRepository.findByIdAndMember(savedWordbook.getId(), savedMember)).willReturn(Optional.of(savedWordbook));
		given(wordRepository.findByWord("apple")).willReturn(List.of(savedWord));
		given(wordbookItemRepository.findByWordbookIdAndWord(savedWordbook.getId(), "apple")).willReturn(Optional.empty());

		wordbookService.addWordCustom(savedWordbook.getId(), dto, savedMember);

		then(wordbookItemRepository).should().save(any(WordbookItem.class));
	}
}