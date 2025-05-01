package com.mallang.mallang_backend.domain.sentence.expressionbook;

import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.savedExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.service.impl.ExpressionBookServiceImpl;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItemId;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository.ExpressionBookItemRepository;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ExpressionBookServiceImplTest {

    @Mock private ExpressionRepository expressionRepository;
    @Mock private ExpressionBookRepository expressionBookRepository;
    @Mock private ExpressionBookItemRepository expressionBookItemRepository;
    @Mock private GptService gptService;
    @Mock private VideoRepository videoRepository;
    @InjectMocks private ExpressionBookServiceImpl expressionBookService;
    private savedExpressionsRequest request;
    private final Long expressionBookId = 1L;

    @BeforeEach
    void setup() {
        request = new savedExpressionsRequest(
                "video123",
                "This is a test sentence.",
                "이것은 테스트 문장입니다.",
                LocalTime.of(0, 1, 5),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("ExpressionBook이 존재하지 않으면 예외")
    void save_shouldThrowExceptionIfExpressionBookNotFound() {
        given(expressionBookRepository.findById(expressionBookId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> expressionBookService.save(request, expressionBookId))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXPRESSION_BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("Expression이 존재하지 않으면 새로 생성하고 저장")
    void save_shouldCreateAndSaveExpressionIfNotExist() {
        given(expressionRepository.findByVideosIdAndSentenceAndSubtitleAt(any(), any(), any())).willReturn(Optional.empty());
        given(expressionBookRepository.findById(expressionBookId)).willReturn(Optional.of(mock(ExpressionBook.class)));
        given(gptService.analyzeSentence(any(), any())).willReturn("분석결과");
        given(videoRepository.findById(any())).willReturn(Optional.of(mock(Videos.class)));
        given(expressionRepository.save(any())).willReturn(mock(Expression.class));
        given(expressionBookItemRepository.existsById(any(ExpressionBookItemId.class))).willReturn(false);

        expressionBookService.save(request, expressionBookId);

        then(expressionRepository).should().save(any());
        then(expressionBookItemRepository).should().save(any(ExpressionBookItem.class));
    }

    @Test
    @DisplayName("이미 존재하는 ExpressionBookItem이 있으면 저장하지 않는다")
    void save_shouldSkipIfAlreadyExists() {
        Expression expression = mock(Expression.class);
        given(expressionRepository.findByVideosIdAndSentenceAndSubtitleAt(any(), any(), any()))
                .willReturn(Optional.of(expression));
        given(expressionBookRepository.findById(expressionBookId)).willReturn(Optional.of(mock(ExpressionBook.class)));
        given(expressionBookItemRepository.existsById(any(ExpressionBookItemId.class))).willReturn(true);

        expressionBookService.save(request, expressionBookId);

        then(expressionBookItemRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("비디오를 찾을 수 없으면 예외")
    void save_shouldThrowExceptionWhenVideoNotFound() {
        given(expressionRepository.findByVideosIdAndSentenceAndSubtitleAt(any(), any(), any())).willReturn(Optional.empty());
        given(expressionBookRepository.findById(expressionBookId)).willReturn(Optional.of(mock(ExpressionBook.class)));
        given(gptService.analyzeSentence(any(), any())).willReturn("분석결과");
        given(videoRepository.findById(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> expressionBookService.save(request, expressionBookId))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VIDEO_ID_SEARCH_FAILED);
    }

    @Test
    @DisplayName("GPT 분석 결과가 없으면 예외")
    void save_shouldThrowExceptionWhenGptFails() {
        given(expressionRepository.findByVideosIdAndSentenceAndSubtitleAt(any(), any(), any())).willReturn(Optional.empty());
        given(expressionBookRepository.findById(expressionBookId)).willReturn(Optional.of(mock(ExpressionBook.class)));
        given(gptService.analyzeSentence(any(), any())).willReturn("");

        assertThatThrownBy(() -> expressionBookService.save(request, expressionBookId))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GPT_RESPONSE_EMPTY);
    }
}
