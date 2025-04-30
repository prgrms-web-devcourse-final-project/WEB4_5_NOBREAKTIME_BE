package com.mallang.mallang_backend.domain.voca.wordbook.service.impl;

import static com.mallang.mallang_backend.global.util.ReflectionTestUtil.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
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
import com.mallang.mallang_backend.domain.member.entity.Subscription;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordToWordbookListRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordToWordbookRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordbookCreateRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.mallang.mallang_backend.domain.voca.wordbookitem.repository.WordbookItemRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ServiceException;

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
		savedMember = Member.builder()
			.language(Language.ENGLISH)
			.build();
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

		savedMember.updateSubscription(Subscription.STANDARD);

		given(wordbookRepository.findByIdAndMember(savedWordbook.getId(), savedMember)).willReturn(Optional.of(savedWordbook));
		given(wordRepository.findByWord("apple")).willReturn(List.of(savedWord));
		given(wordbookItemRepository.findByWordbookIdAndWord(savedWordbook.getId(), "apple")).willReturn(Optional.empty());

		wordbookService.addWordCustom(savedWordbook.getId(), dto, savedMember);

		then(wordbookItemRepository).should().save(any(WordbookItem.class));
	}

	@Test
	@DisplayName("추가 단어장을 생성할 수 있다")
	void createWordbook_success() {
		WordbookCreateRequest request = new WordbookCreateRequest();
		request.setName("My Vocab");

		savedMember.updateSubscription(Subscription.STANDARD);

		Wordbook wordbook = Wordbook.builder()
			.member(savedMember)
			.name(request.getName())
			.language(savedMember.getLanguage())
			.build();
		setId(wordbook, 999L);

		given(wordbookRepository.save(any(Wordbook.class))).willReturn(wordbook);

		Long result = wordbookService.createWordbook(request, savedMember);

		assertThat(result).isEqualTo(999L);
		then(wordbookRepository).should().save(any(Wordbook.class));
	}

	@Test
	@DisplayName("단어장 생성 권한이 없으면 예외가 발생한다")
	void createWordbook_noPermission() {
		WordbookCreateRequest request = new WordbookCreateRequest();
		request.setName("My Vocab");

		savedMember.updateSubscription(Subscription.BASIC);

		ServiceException exception = assertThrows(ServiceException.class, () ->
			wordbookService.createWordbook(request, savedMember)
		);

		assertThat(exception.getMessageCode()).isEqualTo("wordbook.create.failed");
	}

	@Test
	@DisplayName("추가 단어장의 이름을 변경할 수 있다")
	void renameWordbook_success() {
		String newName = "Updated Wordbook Name";
		savedWordbook.updateName("내 단어장"); // 기존 이름이 "기본"이 아님

		given(wordbookRepository.findByIdAndMember(savedWordbook.getId(), savedMember))
			.willReturn(Optional.of(savedWordbook));

		wordbookService.renameWordbook(savedWordbook.getId(), newName, savedMember);

		assertThat(savedWordbook.getName()).isEqualTo(newName);
	}

	@Test
	@DisplayName("단어장 이름 변경 시 단어장이 존재하지 않거나 권한이 없으면 예외가 발생한다")
	void renameWordbook_notFoundOrForbidden() {
		String newName = "Updated Wordbook Name";
		given(wordbookRepository.findByIdAndMember(savedWordbook.getId(), savedMember))
			.willReturn(Optional.empty());

		ServiceException exception = assertThrows(ServiceException.class, () ->
			wordbookService.renameWordbook(savedWordbook.getId(), newName, savedMember)
		);
		assertThat(exception.getMessageCode()).isEqualTo("no.wordbook.exist.or.forbidden");
	}

	@Test
	@DisplayName("\"기본\" 단어장은 생성할 수 없다")
	void createWordbook_failIfNameIsDefault() {
		// given
		WordbookCreateRequest request = new WordbookCreateRequest();
		request.setName("기본");

		savedMember.updateSubscription(Subscription.STANDARD);

		// when & then
		ServiceException exception = assertThrows(ServiceException.class, () ->
			wordbookService.createWordbook(request, savedMember)
		);

		assertThat(exception.getMessageCode()).isEqualTo("wordbook.create.default.forbidden");
	}

	@Test
	@DisplayName("\"기본\" 단어장의 이름은 변경할 수 없다")
	void renameWordbook_failIfDefaultName() {
		// given
		String originalName = "기본";
		String newName = "새 이름";
		savedWordbook.updateName(originalName); // 기존 이름이 "기본"인 상태

		given(wordbookRepository.findByIdAndMember(savedWordbook.getId(), savedMember))
			.willReturn(Optional.of(savedWordbook));

		// when & then
		ServiceException exception = assertThrows(ServiceException.class, () ->
			wordbookService.renameWordbook(savedWordbook.getId(), newName, savedMember)
		);

		assertThat(exception.getMessageCode()).isEqualTo("wordbook.rename.default.forbidden");
	}
}