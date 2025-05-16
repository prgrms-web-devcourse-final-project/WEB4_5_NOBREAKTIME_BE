package com.mallang.mallang_backend.domain.voca.wordbook.service.impl;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.quiz.wordquizresult.repository.WordQuizResultRepository;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.video.subtitle.repository.SubtitleRepository;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.domain.voca.word.entity.Difficulty;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.*;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.mallang.mallang_backend.domain.voca.wordbookitem.repository.WordbookItemRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.mallang.mallang_backend.domain.member.entity.SubscriptionType.STANDARD;
import static com.mallang.mallang_backend.global.constants.AppConstants.DEFAULT_WORDBOOK_NAME;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;
import static com.mallang.mallang_backend.global.util.ReflectionTestUtil.setId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

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

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SubtitleRepository subtitleRepository;

    @Mock
    private WordQuizResultRepository wordQuizResultRepository;

    @Mock
    private VideoRepository videoRepository;

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
        savedMember.updateSubscription(STANDARD);
        setId(savedMember, 1L);

        // Wordbook
        savedDefaultWordBook = Wordbook.builder()
                .name(DEFAULT_WORDBOOK_NAME)
                .language(Language.ENGLISH)
                .build();
        setId(savedDefaultWordBook, 1L);

        savedWordbook = Wordbook.builder()
                .name("추가 단어장1")
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

        given(wordbookRepository.findByIdAndMemberId(savedWordbook.getId(), savedMember.getId())).willReturn(
                Optional.of(savedWordbook));
        given(wordRepository.findByWord("apple")).willReturn(List.of(savedWord));
        given(wordbookItemRepository.findByWordbookIdAndWord(savedWordbook.getId(), "apple")).willReturn(
                Optional.empty());
        given(memberRepository.findById(1L)).willReturn(Optional.of(savedMember));

        wordbookService.addWords(savedWordbook.getId(), request, savedMember.getId());

        then(wordbookItemRepository).should().save(any(WordbookItem.class));
    }

    @Test
    @DisplayName("단어장에 커스텀 단어를 추가할 수 있다")
    void addWordCustom() {
        AddWordRequest dto = new AddWordRequest();
        dto.setWord("apple");

        savedMember.updateSubscription(STANDARD);

        given(wordbookRepository.findByIdAndMemberId(savedWordbook.getId(), savedMember.getId())).willReturn(
                Optional.of(savedWordbook));
        given(wordRepository.findByWord("apple")).willReturn(List.of(savedWord));
        given(wordbookItemRepository.findByWordbookIdAndWord(savedWordbook.getId(), "apple")).willReturn(
                Optional.empty());
        given(memberRepository.findById(1L)).willReturn(Optional.of(savedMember));

        wordbookService.addWordCustom(savedWordbook.getId(), dto, savedMember.getId());

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

            savedMember.updateSubscription(STANDARD);

            Wordbook wordbook = Wordbook.builder()
                    .member(savedMember)
                    .name(request.getName())
                    .language(savedMember.getLanguage())
                    .build();
            setId(wordbook, 999L);

            given(memberRepository.findById(savedMember.getId())).willReturn(Optional.of(savedMember));
            given(wordbookRepository.save(any(Wordbook.class))).willReturn(wordbook);

            Long result = wordbookService.createWordbook(request, savedMember.getId());

            assertThat(result).isEqualTo(999L);
            then(wordbookRepository).should().save(any(Wordbook.class));
        }

        @Test
        @DisplayName("실패 - \"기본\" 단어장은 생성할 수 없다")
        void createWordbook_failIfNameIsDefault() {
            WordbookCreateRequest request = new WordbookCreateRequest();
            request.setName("기본 단어장");

            savedMember.updateSubscription(STANDARD);

            given(memberRepository.findById(savedMember.getId())).willReturn(Optional.of(savedMember));

            ServiceException exception = assertThrows(ServiceException.class, () ->
                    wordbookService.createWordbook(request, savedMember.getId())
            );

            assertThat(exception.getMessageCode()).isEqualTo(WORDBOOK_CREATE_DEFAULT_FORBIDDEN.getMessageCode());
        }

        @Test
        @DisplayName("실패 - 학습 단어를 선택하지 않으면 추가 단어장 생성에 실패한다")
        void createWordbook_language_is_none() {
            WordbookCreateRequest request = new WordbookCreateRequest();
            request.setName("My Vocab");

            savedMember.updateLearningLanguage(Language.NONE);

            given(memberRepository.findById(savedMember.getId())).willReturn(Optional.of(savedMember));

            ServiceException exception = assertThrows(ServiceException.class, () ->
                    wordbookService.createWordbook(request, savedMember.getId())
            );

            assertThat(exception.getMessageCode()).isEqualTo(LANGUAGE_IS_NONE.getMessageCode());
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

            given(wordbookRepository.findByIdAndMemberId(savedWordbook.getId(), savedMember.getId()))
                    .willReturn(Optional.of(savedWordbook));

            wordbookService.renameWordbook(savedWordbook.getId(), newName, savedMember.getId());

            assertThat(savedWordbook.getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("실패 - \"기본\" 단어장의 이름은 변경할 수 없다")
        void renameWordbook_failIfDefaultName() {
            String newName = "새 이름";

            given(wordbookRepository.findByIdAndMemberId(savedDefaultWordBook.getId(), savedMember.getId()))
                    .willReturn(Optional.of(savedDefaultWordBook));

            ServiceException exception = assertThrows(ServiceException.class, () ->
                    wordbookService.renameWordbook(savedDefaultWordBook.getId(), newName, savedMember.getId())
            );

            assertThat(exception.getMessageCode()).isEqualTo(WORDBOOK_RENAME_DEFAULT_FORBIDDEN.getMessageCode());
        }

        @Test
        @DisplayName("실패 - 단어장 이름 변경 시 단어장이 존재하지 않거나 권한이 없으면 예외가 발생한다")
        void renameWordbook_notFoundOrForbidden() {
            String newName = "Updated Wordbook Name";
            given(wordbookRepository.findByIdAndMemberId(savedWordbook.getId(), savedMember.getId()))
                    .willReturn(Optional.empty());

            ServiceException exception = assertThrows(ServiceException.class, () ->
                    wordbookService.renameWordbook(savedWordbook.getId(), newName, savedMember.getId())
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
            given(wordbookRepository.findByIdAndMemberId(savedWordbook.getId(), savedMember.getId()))
                    .willReturn(Optional.of(savedWordbook));

            wordbookService.deleteWordbook(savedWordbook.getId(), savedMember.getId());

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

            given(wordbookRepository.findByIdAndMemberId(wordbookId, savedMember.getId())).willReturn(Optional.of(wordbook));

            wordbookService.deleteWordbook(wordbookId, savedMember.getId());

            then(wordbookItemRepository).should().deleteAllByWordbookId(wordbookId);
            then(wordbookRepository).should().delete(wordbook);
        }

        @Test
        @DisplayName("실패 - 단어장이 존재하지 않거나 권한이 없으면 삭제할 수 없다")
        void deleteWordbook_notFoundOrForbidden() {
            given(wordbookRepository.findByIdAndMemberId(savedWordbook.getId(), savedMember.getId()))
                    .willReturn(Optional.empty());

            ServiceException exception = assertThrows(ServiceException.class, () ->
                    wordbookService.deleteWordbook(savedWordbook.getId(), savedMember.getId())
            );

            assertThat(exception.getMessageCode()).isEqualTo(NO_WORDBOOK_EXIST_OR_FORBIDDEN.getMessageCode());
        }

        @Test
        @DisplayName("실패 - \"기본\" 단어장은 삭제할 수 없다")
        void deleteWordbook_failIfDefault() {
            given(wordbookRepository.findByIdAndMemberId(savedDefaultWordBook.getId(), savedMember.getId()))
                    .willReturn(Optional.of(savedDefaultWordBook));

            ServiceException exception = assertThrows(ServiceException.class, () ->
                    wordbookService.deleteWordbook(savedDefaultWordBook.getId(), savedMember.getId())
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

            given(wordbookRepository.findByIdAndMemberId(toId, savedMember.getId())).willReturn(Optional.of(toWordbook));
            given(wordbookRepository.findByIdAndMemberId(fromId1, savedMember.getId())).willReturn(Optional.of(fromWordbook1));
            given(wordbookRepository.findByIdAndMemberId(fromId2, savedMember.getId())).willReturn(Optional.of(fromWordbook2));

            given(wordbookItemRepository.findByWordbookAndWord(fromWordbook1, "apple")).willReturn(Optional.of(item1));
            given(wordbookItemRepository.findByWordbookAndWord(fromWordbook2, "banana")).willReturn(Optional.of(item2));

            wordbookService.moveWords(request, savedMember.getId());

            then(wordbookItemRepository).should(times(2)).save(any(WordbookItem.class));
        }

        @Test
        @DisplayName("실패 - 목적지 단어장이 존재하지 않거나 접근 권한이 없으면 예외 발생")
        void moveWords_destinationWordbookNotFoundOrForbidden() {
            Long toId = 999L;

            WordMoveRequest request = new WordMoveRequest();
            request.setDestinationWordbookId(toId);
            request.setWords(List.of()); // words는 비워도 됨

            given(wordbookRepository.findByIdAndMemberId(toId, savedMember.getId())).willReturn(Optional.empty());

            ServiceException exception = assertThrows(ServiceException.class, () ->
                    wordbookService.moveWords(request, savedMember.getId())
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

            given(wordbookRepository.findByIdAndMemberId(toId, savedMember.getId())).willReturn(
                    Optional.of(Wordbook.builder().member(savedMember).build()));
            given(wordbookRepository.findByIdAndMemberId(fromId, savedMember.getId())).willReturn(Optional.empty());

            ServiceException exception = assertThrows(ServiceException.class, () ->
                    wordbookService.moveWords(request, savedMember.getId())
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

            given(wordbookRepository.findByIdAndMemberId(toId, savedMember.getId())).willReturn(Optional.of(toWordbook));
            given(wordbookRepository.findByIdAndMemberId(fromId, savedMember.getId())).willReturn(Optional.of(fromWordbook));
            given(wordbookItemRepository.findByWordbookAndWord(fromWordbook, "apple")).willReturn(Optional.empty());

            ServiceException exception = assertThrows(ServiceException.class, () ->
                    wordbookService.moveWords(request, savedMember.getId())
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

            given(wordbookRepository.findByIdAndMemberId(fromId, savedMember.getId())).willReturn(Optional.of(wordbook));

            wordbookService.moveWords(request, savedMember.getId());

            // delete, save 동작하지 않음
            then(wordbookItemRepository).should(never()).delete(any());
            then(wordbookItemRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("단어장에서 단어 삭제")
    class DeleteWordBookInWord {
        @BeforeEach
        void setUp() {
            savedMember = Member.builder()
                    .language(Language.ENGLISH)
                    .build();
            savedMember.updateSubscription(STANDARD);
            setId(savedMember, 1L);
        }

        @Test
        @DisplayName("성공 - 단어장에 존재하는 단어를 삭제할 수 있다")
        void deleteWords_success() {
            Long wordbookId = 1L;

            Wordbook wordbook = Wordbook.builder()
                    .name(DEFAULT_WORDBOOK_NAME)
                    .member(savedMember)
                    .build();
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

            given(wordbookRepository.findByIdAndMemberId(wordbookId, savedMember.getId())).willReturn(Optional.of(wordbook));
            given(wordbookItemRepository.findByWordbookAndWord(wordbook, "apple")).willReturn(Optional.of(item));
            given(memberRepository.findById(savedMember.getId())).willReturn(Optional.of(savedMember));

            wordbookService.deleteWords(request, savedMember.getId());

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

            given(wordbookRepository.findByIdAndMemberId(wordbookId, savedMember.getId())).willReturn(Optional.empty());
            given(memberRepository.findById(savedMember.getId())).willReturn(Optional.of(savedMember));

            ServiceException exception = assertThrows(ServiceException.class, () ->
                    wordbookService.deleteWords(request, savedMember.getId())
            );

            assertThat(exception.getMessageCode()).isEqualTo(NO_WORDBOOK_EXIST_OR_FORBIDDEN.getMessageCode());
        }

        @Test
        @DisplayName("예외 - 단어장이 존재하지만 해당 단어가 없을 경우 예외 발생")
        void deleteWords_wordNotFound() {
            Long wordbookId = 1L;

            Wordbook wordbook = Wordbook.builder()
                    .name(DEFAULT_WORDBOOK_NAME)
                    .member(savedMember)
                    .build();
            setId(wordbook, wordbookId);

            WordDeleteItem deleteItem = new WordDeleteItem();
            deleteItem.setWordbookId(wordbookId);
            deleteItem.setWord("apple");

            WordDeleteRequest request = new WordDeleteRequest();
            request.setWords(List.of(deleteItem));

            given(wordbookRepository.findByIdAndMemberId(wordbookId, savedMember.getId())).willReturn(Optional.of(wordbook));
            given(wordbookItemRepository.findByWordbookAndWord(wordbook, "apple")).willReturn(Optional.empty());
            given(memberRepository.findById(savedMember.getId())).willReturn(Optional.of(savedMember));

            ServiceException exception = assertThrows(ServiceException.class, () ->
                    wordbookService.deleteWords(request, savedMember.getId())
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

            Wordbook wordbook = Wordbook.builder()
                    .name(DEFAULT_WORDBOOK_NAME)
                    .member(savedMember)
                    .build();
            setId(wordbook, wordbookId);

            WordbookItem item1 = WordbookItem.builder()
                    .word("apple")
                    .videoId("ABCDE")
                    .subtitleId(1L)
                    .wordbook(wordbook)
                    .build();
            setField(item1, "createdAt", LocalDateTime.now());

            WordbookItem item2 = WordbookItem.builder()
                    .word("banana")
                    .videoId("BBBBB")
                    .subtitleId(2L)
                    .wordbook(wordbook)
                    .build();
            setField(item2, "createdAt", LocalDateTime.now());

            List<WordbookItem> items = List.of(item1, item2);

            // Word 엔티티 (exampleSentence는 subtitle로 덮어씌워질 예정)
            Word word1 = Word.builder()
                    .word("apple")
                    .pos("noun")
                    .meaning("사과")
                    .difficulty(Difficulty.EASY)
                    .exampleSentence("I like apple.")
                    .translatedSentence("나는 사과를 좋아해.")
                    .build();

            Word word2 = Word.builder()
                    .word("banana")
                    .pos("noun")
                    .meaning("바나나")
                    .difficulty(Difficulty.NORMAL)
                    .exampleSentence("Bananas are yellow.")
                    .translatedSentence("바나나는 노랗다.")
                    .build();

            // Subtitle 엔티티
            Subtitle subtitle1 = Subtitle.builder()
                    .originalSentence("Apple is red.")
                    .translatedSentence("사과는 빨갛다.")
                    .build();
            setId(subtitle1, 1L);

            Subtitle subtitle2 = Subtitle.builder()
                    .originalSentence("Banana is yellow.")
                    .translatedSentence("바나나는 노랗다.")
                    .build();
            setId(subtitle2, 2L);

            Videos videos1 = Videos.builder()
                    .id("ABCDE")
                    .videoTitle("example1")
                    .thumbnailImageUrl("https://i.ytimg.com/vi/OGz4EJIUPiA/mqdefault.jpg")
                    .language(Language.ENGLISH)
                    .build();

            Videos videos2 = Videos.builder()
                    .id("BBBBB")
                    .videoTitle("example1")
                    .thumbnailImageUrl("https://i.ytimg.com/vi/OGz4EJIUPiA/mqdefault.jpg")
                    .language(Language.ENGLISH)
                    .build();

            given(wordbookRepository.findByIdAndMemberId(wordbookId, savedMember.getId()))
                    .willReturn(Optional.of(wordbook));

            given(wordbookItemRepository.findAllByWordbook(wordbook))
                    .willReturn(items);

            given(wordRepository.findByWordIn(List.of("apple", "banana")))
                    .willReturn(List.of(word1, word2));

            given(subtitleRepository.findByIdIn(List.of(1L, 2L)))
                    .willReturn(List.of(subtitle1, subtitle2));

            given(videoRepository.findByIdIn(List.of("ABCDE", "BBBBB")))
                    .willReturn(List.of(videos1, videos2));

            given(memberRepository.findById(savedMember.getId())).willReturn(Optional.of(savedMember));

            List<WordResponse> result = wordbookService.getWordsRandomly(wordbookId, savedMember.getId());

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting("word")
                    .containsExactlyInAnyOrder("apple", "banana");

            // 각각 단어별 subtitle 덮어씌운 문장 검증
            for (WordResponse res : result) {
                if (res.getWord().equals("apple")) {
                    assertThat(res.getExampleSentence()).isEqualTo("Apple is red.");
                    assertThat(res.getTranslatedSentence()).isEqualTo("사과는 빨갛다.");
                    assertThat(res.getDifficulty()).isEqualTo(Difficulty.EASY.toString());
                    assertThat(res.getPos()).isEqualTo("noun");
                }
                if (res.getWord().equals("banana")) {
                    assertThat(res.getExampleSentence()).isEqualTo("Banana is yellow.");
                    assertThat(res.getTranslatedSentence()).isEqualTo("바나나는 노랗다.");
                    assertThat(res.getDifficulty()).isEqualTo(Difficulty.NORMAL.toString());
                    assertThat(res.getPos()).isEqualTo("noun");
                }
            }
        }

        @Test
        @DisplayName("예외 - 단어장이 존재하지 않거나 권한이 없을 경우 단어장 조회에 실패한다")
        void getWordsRandomly_wordbookNotFound() {
            Long wordbookId = 99L;

            given(wordbookRepository.findByIdAndMemberId(wordbookId, savedMember.getId())).willReturn(Optional.empty());

            ServiceException exception = assertThrows(ServiceException.class, () ->
                    wordbookService.getWordsRandomly(wordbookId, savedMember.getId())
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

        given(memberRepository.findById(savedMember.getId())).willReturn(Optional.of(savedMember));
        given(wordbookRepository.findAllByMemberIdAndLanguage(savedMember.getId(), savedMember.getLanguage())).willReturn(List.of(wb1, wb2));

        List<WordbookResponse> result = wordbookService.getWordbooks(savedMember.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("My First Book");
        assertThat(result.get(1).getName()).isEqualTo("TOEIC Book");
    }
}