package com.mallang.mallang_backend.domain.sentence.expressionbook.service;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionSaveRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.service.impl.ExpressionBookServiceImpl;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItemId;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository.ExpressionBookItemRepository;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.video.subtitle.repository.SubtitleRepository;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.mallang.mallang_backend.global.constants.AppConstants.DEFAULT_WORDBOOK_NAME;
import static com.mallang.mallang_backend.global.util.ReflectionTestUtil.setId;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ExpressionBookServiceImplTest {
    @Mock
    private ExpressionRepository expressionRepository;

    @Mock
    private ExpressionBookRepository expressionBookRepository;

    @Mock
    private ExpressionBookItemRepository expressionBookItemRepository;

    @Mock
    private GptService gptService;

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private SubtitleRepository subtitleRepository;

    @Mock
    private MemberRepository memberRepostiroy;

    @InjectMocks
    private ExpressionBookServiceImpl expressionBookService;

    private ExpressionSaveRequest request;

    private final String videoId = "ABCDEF123";

    private Subtitle subtitle;

    private Member member;

    private ExpressionBook expressionBook;

    @BeforeEach
    void setup() {
        member = Member.builder()
                .language(Language.ENGLISH)
                .build();
        member.updateSubscription(SubscriptionType.STANDARD);
        setId(member, 1L);

        subtitle = Subtitle.builder()
                .startTime("00:10:07.500")
                .originalSentence("I like banana.")
                .translatedSentence("나는 바나나를 좋아해.")
                .build();
        setId(subtitle, 1L);

        expressionBook = ExpressionBook.builder()
                .name(DEFAULT_WORDBOOK_NAME)
                .member(member)
                .language(Language.ENGLISH)
                .build();
        setId(expressionBook, 1L);

        request = new ExpressionSaveRequest(videoId, subtitle.getId());
    }

    @Test
    @DisplayName("ExpressionBook이 존재하지 않으면 예외")
    void save_shouldThrowExceptionIfExpressionBookNotFound() {
        given(expressionBookRepository.findById(expressionBook.getId())).willReturn(Optional.empty());
        given(memberRepostiroy.findById(member.getId())).willReturn(Optional.of(member));

        assertThatThrownBy(() -> expressionBookService.save(request, expressionBook.getId(), member.getId()))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXPRESSION_BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("Expression이 존재하지 않으면 새로 생성하고 저장")
    void save_shouldCreateAndSaveExpressionIfNotExist() {
        given(subtitleRepository.findById(subtitle.getId())).willReturn(Optional.of(subtitle));
        given(expressionRepository.findByVideosIdAndSentenceAndSubtitleAt(any(), any(), any())).willReturn(Optional.empty());
        given(expressionBookRepository.findById(expressionBook.getId())).willReturn(Optional.of(expressionBook));
        given(gptService.analyzeSentence(any(), any(), any())).willReturn("분석결과");
        given(videoRepository.findById(any())).willReturn(Optional.of(mock(Videos.class)));
        given(expressionRepository.save(any())).willReturn(mock(Expression.class));
        given(expressionBookItemRepository.existsById(any(ExpressionBookItemId.class))).willReturn(false);
        given(memberRepostiroy.findById(member.getId())).willReturn(Optional.of(member));

        expressionBookService.save(request, expressionBook.getId(), member.getId());

        then(expressionRepository).should().save(any());
        then(expressionBookItemRepository).should().save(any(ExpressionBookItem.class));
    }

    @Test
    @DisplayName("이미 존재하는 ExpressionBookItem이 있으면 저장하지 않는다")
    void save_shouldSkipIfAlreadyExists() {

        ExpressionBook expressionBook = ExpressionBook.builder()
                .name(DEFAULT_WORDBOOK_NAME)
                .member(member)
                .language(Language.ENGLISH)
                .build();
        setId(expressionBook, 1L);

        given(subtitleRepository.findById(subtitle.getId())).willReturn(Optional.of(subtitle));
        Expression expression = mock(Expression.class);
        given(expressionRepository.findByVideosIdAndSentenceAndSubtitleAt(any(), any(), any()))
                .willReturn(Optional.of(expression));
        given(expressionBookRepository.findById(expressionBook.getId())).willReturn(Optional.of(expressionBook));
        given(expressionBookItemRepository.existsById(any(ExpressionBookItemId.class))).willReturn(true);
        given(memberRepostiroy.findById(member.getId())).willReturn(Optional.of(member));

        expressionBookService.save(request, expressionBook.getId(), member.getId());

        then(expressionBookItemRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("비디오를 찾을 수 없으면 예외")
    void save_shouldThrowExceptionWhenVideoNotFound() {
        given(subtitleRepository.findById(subtitle.getId())).willReturn(Optional.of(subtitle));
        given(expressionRepository.findByVideosIdAndSentenceAndSubtitleAt(any(), any(), any())).willReturn(Optional.empty());
        given(expressionBookRepository.findById(expressionBook.getId())).willReturn(Optional.of(expressionBook));
        given(gptService.analyzeSentence(any(), any(), any())).willReturn("분석결과");
        given(videoRepository.findById(any())).willReturn(Optional.empty());
        given(memberRepostiroy.findById(member.getId())).willReturn(Optional.of(member));

        assertThatThrownBy(() -> expressionBookService.save(request, expressionBook.getId(), member.getId()))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VIDEO_ID_SEARCH_FAILED);
    }

    @Test
    @DisplayName("GPT 분석 결과가 없으면 예외")
    void save_shouldThrowExceptionWhenGptFails() {
        given(subtitleRepository.findById(subtitle.getId())).willReturn(Optional.of(subtitle));
        given(expressionRepository.findByVideosIdAndSentenceAndSubtitleAt(any(), any(), any())).willReturn(Optional.empty());
        given(expressionBookRepository.findById(expressionBook.getId())).willReturn(Optional.of(expressionBook));
        given(gptService.analyzeSentence(any(), any(), any())).willReturn("");
        given(memberRepostiroy.findById(member.getId())).willReturn(Optional.of(member));

        assertThatThrownBy(() -> expressionBookService.save(request, expressionBook.getId(), member.getId()))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GPT_RESPONSE_EMPTY);
    }
}
