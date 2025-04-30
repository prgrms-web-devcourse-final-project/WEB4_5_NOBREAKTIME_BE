package com.mallang.mallang_backend.domain.voca.wordbook.service.impl;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;
import static com.mallang.mallang_backend.global.util.ReflectionTestUtil.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordDeleteItem;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordDeleteRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordMoveItem;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordMoveRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordResponse;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordbookCreateRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordbookResponse;
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
	private Wordbook savedDefaultWordBook;
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
		savedDefaultWordBook = Wordbook.builder()
			.name(DEFAULT_WORDBOOK_NAME)
			.language(Language.ENGLISH)
			.build();
		setId(savedDefaultWordBook, 1L);

		savedWordbook = Wordbook.builder()
			.member(savedMember)
			.build();
		setId(savedWordbook, 100L);

		// Word
		savedWord = Word.builder()
			.word("apple")
			.build();
		setId(savedWord, 200L);
	}

	@Test
	@DisplayName("단어장에 단어를 추가할 수 있다")
	void addWords() {
		AddWordToWordbookRequest dto = new AddWordToWordbookRequest();
		dto.setWord("apple");
		dto.setVideoId("ABCDE");
		dto.setSubtitleId(20L);

		AddWordToWordbookListRequest request = new AddWordToWordbookListRequest();
		request.setWords(List.of(dto));

		given(wordbookRepository.findByIdAndMember(savedWordbook.getId(), savedMember)).willReturn(
			Optional.of(savedWordbook));
		given(wordRepository.findByWord("apple")).willReturn(List.of(savedWord));
		given(wordbookItemRepository.findByWordbookIdAndWord(savedWordbook.getId(), "apple")).willReturn(
			Optional.empty());

		wordbookService.addWords(savedWordbook.getId(), request, savedMember);

		then(wordbookItemRepository).should().save(any(WordbookItem.class));
	}

	@Test
	@DisplayName("단어장에 커스텀 단어를 추가할 수 있다")
	void addWordCustom() {
		AddWordRequest dto = new AddWordRequest();
		dto.setWord("apple");

		savedMember.updateSubscription(Subscription.STANDARD);

		given(wordbookRepository.findByIdAndMember(savedWordbook.getId(), savedMember)).willReturn(
			Optional.of(savedWordbook));
		given(wordRepository.findByWord("apple")).willReturn(List.of(savedWord));
		given(wordbookItemRepository.findByWordbookIdAndWord(savedWordbook.getId(), "apple")).willReturn(
			Optional.empty());

		wordbookService.addWordCustom(savedWordbook.getId(), dto, savedMember);

		then(wordbookItemRepository).should().save(any(WordbookItem.class));
	}

	@Nested
	@DisplayName("추가 단어장 생성")
	class CreateWordBook {
		@Test
		@DisplayName("성공 - 추가 단어장을 생성할 수 있다")
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
		@DisplayName("실패 - 단어장 생성 권한이 없으면 예외가 발생한다")
		void createWordbook_noPermission() {
			WordbookCreateRequest request = new WordbookCreateRequest();
			request.setName("My Vocab");

			savedMember.updateSubscription(Subscription.BASIC);

			ServiceException exception = assertThrows(ServiceException.class, () ->
				wordbookService.createWordbook(request, savedMember)
			);

			assertThat(exception.getMessageCode()).isEqualTo(NO_WORDBOOK_CREATE_PERMISSION.getMessageCode());
		}

		@Test
		@DisplayName("실패 - \"기본\" 단어장은 생성할 수 없다")
		void createWordbook_failIfNameIsDefault() {
			WordbookCreateRequest request = new WordbookCreateRequest();
			request.setName("기본");

			savedMember.updateSubscription(Subscription.STANDARD);

			ServiceException exception = assertThrows(ServiceException.class, () ->
				wordbookService.createWordbook(request, savedMember)
			);

			assertThat(exception.getMessageCode()).isEqualTo(WORDBOOK_CREATE_DEFAULT_FORBIDDEN.getMessageCode());
		}
	}

	@Nested
	@DisplayName("추가 단어장 이름 변경")
	class RenameWordbook {
		@Test
		@DisplayName("성공 - 추가 단어장의 이름을 변경할 수 있다")
		void renameWordbook_success() {
			String newName = "Updated Wordbook Name";
			savedWordbook.updateName("내 단어장"); // 기존 이름이 "기본"이 아님

			given(wordbookRepository.findByIdAndMember(savedWordbook.getId(), savedMember))
				.willReturn(Optional.of(savedWordbook));

			wordbookService.renameWordbook(savedWordbook.getId(), newName, savedMember);

			assertThat(savedWordbook.getName()).isEqualTo(newName);
		}

		@Test
		@DisplayName("실패 - \"기본\" 단어장의 이름은 변경할 수 없다")
		void renameWordbook_failIfDefaultName() {
			String newName = "새 이름";

			given(wordbookRepository.findByIdAndMember(savedDefaultWordBook.getId(), savedMember))
				.willReturn(Optional.of(savedDefaultWordBook));

			ServiceException exception = assertThrows(ServiceException.class, () ->
				wordbookService.renameWordbook(savedDefaultWordBook.getId(), newName, savedMember)
			);

			assertThat(exception.getMessageCode()).isEqualTo(WORDBOOK_RENAME_DEFAULT_FORBIDDEN.getMessageCode());
		}

		@Test
		@DisplayName("실패 - 단어장 이름 변경 시 단어장이 존재하지 않거나 권한이 없으면 예외가 발생한다")
		void renameWordbook_notFoundOrForbidden() {
			String newName = "Updated Wordbook Name";
			given(wordbookRepository.findByIdAndMember(savedWordbook.getId(), savedMember))
				.willReturn(Optional.empty());

			ServiceException exception = assertThrows(ServiceException.class, () ->
				wordbookService.renameWordbook(savedWordbook.getId(), newName, savedMember)
			);
			assertThat(exception.getMessageCode()).isEqualTo(NO_WORDBOOK_EXIST_OR_FORBIDDEN.getMessageCode());
		}
	}

	@Nested
	@DisplayName("추가 단어장 삭제")
	class DeleteWordbook {
		@Test
		@DisplayName("성공 - 추가 단어장을 삭제할 수 있다")
		void deleteWordbook_success() {
			given(wordbookRepository.findByIdAndMember(savedWordbook.getId(), savedMember))
				.willReturn(Optional.of(savedWordbook));

			wordbookService.deleteWordbook(savedWordbook.getId(), savedMember);

			then(wordbookRepository).should().delete(savedWordbook);
		}

		@Test
		@DisplayName("성공 - 추가 단어장 삭제 시 해당 단어 아이템도 함께 삭제된다")
		void deleteWordbookAlsoDeletesItems() {
			Long wordbookId = 1L;

			Wordbook wordbook = Wordbook.builder()
				.name("MyWordbook")
				.member(savedMember)
				.build();
			setId(wordbook, wordbookId);

			WordbookItem item1 = WordbookItem.builder()
				.wordbook(wordbook)
				.word("apple")
				.subtitleId(101L)
				.videoId("ABC1")
				.build();
			setId(item1, 1001L);

			WordbookItem item2 = WordbookItem.builder()
				.wordbook(wordbook)
				.word("banana")
				.subtitleId(102L)
				.videoId("ABC2")
				.build();
			setId(item2, 1002L);

			given(wordbookRepository.findByIdAndMember(wordbookId, savedMember)).willReturn(Optional.of(wordbook));

			wordbookService.deleteWordbook(wordbookId, savedMember);

			then(wordbookItemRepository).should().deleteAllByWordbookId(wordbookId);
			then(wordbookRepository).should().delete(wordbook);
		}

		@Test
		@DisplayName("실패 - 단어장이 존재하지 않거나 권한이 없으면 삭제할 수 없다")
		void deleteWordbook_notFoundOrForbidden() {
			given(wordbookRepository.findByIdAndMember(savedWordbook.getId(), savedMember))
				.willReturn(Optional.empty());

			ServiceException exception = assertThrows(ServiceException.class, () ->
				wordbookService.deleteWordbook(savedWordbook.getId(), savedMember)
			);

			assertThat(exception.getMessageCode()).isEqualTo(NO_WORDBOOK_EXIST_OR_FORBIDDEN.getMessageCode());
		}

		@Test
		@DisplayName("실패 - \"기본\" 단어장은 삭제할 수 없다")
		void deleteWordbook_failIfDefault() {
			given(wordbookRepository.findByIdAndMember(savedDefaultWordBook.getId(), savedMember))
				.willReturn(Optional.of(savedDefaultWordBook));

			ServiceException exception = assertThrows(ServiceException.class, () ->
				wordbookService.deleteWordbook(savedDefaultWordBook.getId(), savedMember)
			);

			assertThat(exception.getMessageCode()).isEqualTo(WORDBOOK_DELETE_DEFAULT_FORBIDDEN.getMessageCode());
		}
	}

	@Nested
	@DisplayName("단어장 단어 이동")
	class MoveWordBook {
		@Test
		@DisplayName("성공 - 단어들을 다른 단어장으로 이동할 수 있다")
		void moveWords_success() {
			Long fromId1 = 1L;
			Long fromId2 = 2L;
			Long toId = 10L;

			// destination 단어장
			Wordbook toWordbook = Wordbook.builder().member(savedMember).build();
			setId(toWordbook, toId);

			// source 단어장 1, 2
			Wordbook fromWordbook1 = Wordbook.builder().member(savedMember).build();
			Wordbook fromWordbook2 = Wordbook.builder().member(savedMember).build();
			setId(fromWordbook1, fromId1);
			setId(fromWordbook2, fromId2);

			// 기존 WordbookItem
			WordbookItem item1 = WordbookItem.builder()
				.wordbook(fromWordbook1)
				.word("apple")
				.subtitleId(100L)
				.videoId("ABCDE")
				.build();
			setId(item1, 1000L);

			WordbookItem item2 = WordbookItem.builder()
				.wordbook(fromWordbook2)
				.word("banana")
				.subtitleId(110L)
				.videoId("BBBBB")
				.build();
			setId(item2, 1001L);

			// 이동 요청 DTO
			WordMoveRequest request = new WordMoveRequest();
			request.setDestinationWordbookId(toId);

			WordMoveItem wordMoveItem1 = new WordMoveItem();
			wordMoveItem1.setFromWordbookId(fromId1);
			wordMoveItem1.setWord("apple");

			WordMoveItem wordMoveItem2 = new WordMoveItem();
			wordMoveItem2.setFromWordbookId(fromId2);
			wordMoveItem2.setWord("banana");

			request.setWords(List.of(
				wordMoveItem1,
				wordMoveItem2
			));

			given(wordbookRepository.findByIdAndMember(toId, savedMember)).willReturn(Optional.of(toWordbook));
			given(wordbookRepository.findByIdAndMember(fromId1, savedMember)).willReturn(Optional.of(fromWordbook1));
			given(wordbookRepository.findByIdAndMember(fromId2, savedMember)).willReturn(Optional.of(fromWordbook2));

			given(wordbookItemRepository.findByWordbookAndWord(fromWordbook1, "apple")).willReturn(Optional.of(item1));
			given(wordbookItemRepository.findByWordbookAndWord(fromWordbook2, "banana")).willReturn(Optional.of(item2));

			wordbookService.moveWords(request, savedMember);

			then(wordbookItemRepository).should(times(2)).delete(any(WordbookItem.class));
			then(wordbookItemRepository).should(times(2)).save(any(WordbookItem.class));
		}

		@Test
		@DisplayName("실패 - 목적지 단어장이 존재하지 않거나 접근 권한이 없으면 예외 발생")
		void moveWords_destinationWordbookNotFoundOrForbidden() {
			Long toId = 999L;

			WordMoveRequest request = new WordMoveRequest();
			request.setDestinationWordbookId(toId);
			request.setWords(List.of()); // words는 비워도 됨

			given(wordbookRepository.findByIdAndMember(toId, savedMember)).willReturn(Optional.empty());

			ServiceException exception = assertThrows(ServiceException.class, () ->
				wordbookService.moveWords(request, savedMember)
			);

			assertThat(exception.getMessageCode()).isEqualTo(NO_WORDBOOK_EXIST_OR_FORBIDDEN.getMessageCode());
		}

		@Test
		@DisplayName("실패 - 출발 단어장이 존재하지 않거나 접근 권한이 없으면 예외 발생")
		void moveWords_sourceWordbookNotFoundOrForbidden() {
			Long fromId = 1L;
			Long toId = 10L;

			WordMoveRequest request = new WordMoveRequest();
			request.setDestinationWordbookId(toId);

			WordMoveItem wordMoveItem = new WordMoveItem();
			wordMoveItem.setFromWordbookId(fromId);
			wordMoveItem.setWord("apple");

			request.setWords(List.of(wordMoveItem));

			given(wordbookRepository.findByIdAndMember(toId, savedMember)).willReturn(
				Optional.of(Wordbook.builder().member(savedMember).build()));
			given(wordbookRepository.findByIdAndMember(fromId, savedMember)).willReturn(Optional.empty());

			ServiceException exception = assertThrows(ServiceException.class, () ->
				wordbookService.moveWords(request, savedMember)
			);

			assertThat(exception.getMessageCode()).isEqualTo(NO_WORDBOOK_EXIST_OR_FORBIDDEN.getMessageCode());
		}

		@Test
		@DisplayName("실패 - 출발 단어장에 단어가 없으면 예외 발생")
		void moveWords_wordNotFoundInSourceWordbook() {
			Long fromId = 1L;
			Long toId = 10L;

			Wordbook fromWordbook = Wordbook.builder().member(savedMember).build();
			setId(fromWordbook, fromId);

			Wordbook toWordbook = Wordbook.builder().member(savedMember).build();
			setId(toWordbook, toId);

			WordMoveRequest request = new WordMoveRequest();
			request.setDestinationWordbookId(toId);

			WordMoveItem wordMoveItem = new WordMoveItem();
			wordMoveItem.setFromWordbookId(fromId);
			wordMoveItem.setWord("apple");

			request.setWords(List.of(wordMoveItem));

			given(wordbookRepository.findByIdAndMember(toId, savedMember)).willReturn(Optional.of(toWordbook));
			given(wordbookRepository.findByIdAndMember(fromId, savedMember)).willReturn(Optional.of(fromWordbook));
			given(wordbookItemRepository.findByWordbookAndWord(fromWordbook, "apple")).willReturn(Optional.empty());

			ServiceException exception = assertThrows(ServiceException.class, () ->
				wordbookService.moveWords(request, savedMember)
			);

			assertThat(exception.getMessageCode()).isEqualTo(WORDBOOK_ITEM_NOT_FOUND.getMessageCode());
		}

		@Test
		@DisplayName("성공 - 같은 단어장으로 이동하려는 경우 무시하고 넘어간다")
		void moveWords_sameWordbookIgnored() {
			Long fromId = 1L;

			Wordbook wordbook = Wordbook.builder().member(savedMember).build();
			setId(wordbook, fromId);

			WordMoveRequest request = new WordMoveRequest();
			request.setDestinationWordbookId(fromId); // 동일

			WordMoveItem wordMoveItem = new WordMoveItem();
			wordMoveItem.setFromWordbookId(fromId);
			wordMoveItem.setWord("apple");

			request.setWords(List.of(wordMoveItem));

			given(wordbookRepository.findByIdAndMember(fromId, savedMember)).willReturn(Optional.of(wordbook));

			wordbookService.moveWords(request, savedMember);

			// delete, save 동작하지 않음
			then(wordbookItemRepository).should(never()).delete(any());
			then(wordbookItemRepository).should(never()).save(any());
		}
	}

	@Nested
	@DisplayName("단어장에서 단어 삭제")
	class DeleteWordBookInWord {
		@Test
		@DisplayName("성공 - 단어장에 존재하는 단어를 삭제할 수 있다")
		void deleteWords_success() {
			Long wordbookId = 1L;

			Wordbook wordbook = Wordbook.builder().member(savedMember).build();
			setId(wordbook, wordbookId);

			WordbookItem item = WordbookItem.builder()
				.wordbook(wordbook)
				.word("apple")
				.subtitleId(100L)
				.videoId("ABCDE")
				.build();
			setId(item, 1000L);

			WordDeleteItem deleteItem = new WordDeleteItem();
			deleteItem.setWordbookId(wordbookId);
			deleteItem.setWord("apple");

			WordDeleteRequest request = new WordDeleteRequest();
			request.setWords(List.of(deleteItem));

			given(wordbookRepository.findByIdAndMember(wordbookId, savedMember)).willReturn(Optional.of(wordbook));
			given(wordbookItemRepository.findByWordbookAndWord(wordbook, "apple")).willReturn(Optional.of(item));

			wordbookService.deleteWords(request, savedMember);

			then(wordbookItemRepository).should().delete(item);
		}

		@Test
		@DisplayName("예외 - 단어장이 존재하지 않거나 권한이 없을 경우 예외 발생")
		void deleteWords_wordbookNotFound() {
			Long wordbookId = 1L;

			WordDeleteItem deleteItem = new WordDeleteItem();
			deleteItem.setWordbookId(wordbookId);
			deleteItem.setWord("apple");

			WordDeleteRequest request = new WordDeleteRequest();
			request.setWords(List.of(deleteItem));

			given(wordbookRepository.findByIdAndMember(wordbookId, savedMember)).willReturn(Optional.empty());

			ServiceException exception = assertThrows(ServiceException.class, () ->
				wordbookService.deleteWords(request, savedMember)
			);

			assertThat(exception.getMessageCode()).isEqualTo(NO_WORDBOOK_EXIST_OR_FORBIDDEN.getMessageCode());
		}

		@Test
		@DisplayName("예외 - 단어장이 존재하지만 해당 단어가 없을 경우 예외 발생")
		void deleteWords_wordNotFound() {
			Long wordbookId = 1L;

			Wordbook wordbook = Wordbook.builder().member(savedMember).build();
			setId(wordbook, wordbookId);

			WordDeleteItem deleteItem = new WordDeleteItem();
			deleteItem.setWordbookId(wordbookId);
			deleteItem.setWord("apple");

			WordDeleteRequest request = new WordDeleteRequest();
			request.setWords(List.of(deleteItem));

			given(wordbookRepository.findByIdAndMember(wordbookId, savedMember)).willReturn(Optional.of(wordbook));
			given(wordbookItemRepository.findByWordbookAndWord(wordbook, "apple")).willReturn(Optional.empty());

			ServiceException exception = assertThrows(ServiceException.class, () ->
				wordbookService.deleteWords(request, savedMember)
			);

			assertThat(exception.getMessageCode()).isEqualTo(WORDBOOK_ITEM_NOT_FOUND.getMessageCode());
		}
	}

	@Nested
	@DisplayName("단어장 단어 조회")
	class getWordbookInWords {
		@Test
		@DisplayName("성공 - 단어장 내 단어들을 무작위로 조회한다")
		void getWordsRandomly_success() {
			Long wordbookId = 1L;

			Wordbook wordbook = Wordbook.builder().member(savedMember).build();
			setId(wordbook, wordbookId);

			WordbookItem item1 = WordbookItem.builder()
				.word("apple")
				.videoId("ABCDE")
				.subtitleId(1L)
				.wordbook(wordbook)
				.build();

			WordbookItem item2 = WordbookItem.builder()
				.word("banana")
				.videoId("BBBBB")
				.subtitleId(2L)
				.wordbook(wordbook)
				.build();

			List<WordbookItem> items = new ArrayList<>(List.of(item1, item2));

			given(wordbookRepository.findByIdAndMember(wordbookId, savedMember)).willReturn(Optional.of(wordbook));
			given(wordbookItemRepository.findAllByWordbook(wordbook)).willReturn(items);

			List<WordResponse> result = wordbookService.getWordsRandomly(wordbookId, savedMember);

			assertThat(result).hasSize(2);
			assertThat(result)
				.extracting("word")
				.containsExactlyInAnyOrder("apple", "banana");
		}

		@Test
		@DisplayName("예외 - 단어장이 존재하지 않거나 권한이 없을 경우 단어장 조회에 실패한다")
		void getWordsRandomly_wordbookNotFound() {
			Long wordbookId = 99L;

			given(wordbookRepository.findByIdAndMember(wordbookId, savedMember)).willReturn(Optional.empty());

			ServiceException exception = assertThrows(ServiceException.class, () ->
				wordbookService.getWordsRandomly(wordbookId, savedMember)
			);

			assertThat(exception.getMessageCode()).isEqualTo(NO_WORDBOOK_EXIST_OR_FORBIDDEN.getMessageCode());
		}
	}

	@Test
	@DisplayName("회원의 단어장 목록을 조회할 수 있다")
	void getWordbooks_success() {
		Wordbook wb1 = Wordbook.builder()
			.member(savedMember)
			.name("My First Book")
			.language(Language.ENGLISH)
			.build();
		setId(wb1, 1L);

		Wordbook wb2 = Wordbook.builder()
			.member(savedMember)
			.name("TOEIC Book")
			.language(Language.ENGLISH)
			.build();
		setId(wb2, 2L);

		given(wordbookRepository.findAllByMember(savedMember)).willReturn(List.of(wb1, wb2));

		List<WordbookResponse> result = wordbookService.getWordbooks(savedMember);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getName()).isEqualTo("My First Book");
		assertThat(result.get(1).getName()).isEqualTo("TOEIC Book");
	}
}