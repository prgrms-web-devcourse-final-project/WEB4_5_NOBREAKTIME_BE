package com.mallang.mallang_backend.domain.sentence.expressionbookItem;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.AddExpressionToBookListRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.AddExpressionToBookRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.DeleteExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.MoveExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItemId;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository.ExpressionBookItemRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.service.impl.ExpressionBookItemServiceImpl;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ExpressionBookItemServiceImplTest {

    private ExpressionBookItemServiceImpl service;

    private ExpressionBookRepository expressionBookRepository;
    private ExpressionRepository expressionRepository;
    private ExpressionBookItemRepository expressionBookItemRepository;
    private VideoRepository videoRepository;

    @BeforeEach
    void setUp() {
        expressionBookRepository = mock(ExpressionBookRepository.class);
        expressionRepository = mock(ExpressionRepository.class);
        expressionBookItemRepository = mock(ExpressionBookItemRepository.class);
        videoRepository = mock(VideoRepository.class);

        service = new ExpressionBookItemServiceImpl(
                expressionBookRepository,
                expressionRepository,
                expressionBookItemRepository,
                videoRepository
        );
    }

    @Test
    @DisplayName("표현을 영상에서 표현함에 추가할 수 있다")
    void addExpressionsFromVideo_success() throws Exception {
        Long bookId = 1L;
        Member member = mockMember(1L);
        ExpressionBook book = mockBook(bookId, member);

        AddExpressionToBookRequest expReq = new AddExpressionToBookRequest();
        expReq.setSentence("Test");
        expReq.setDescription("desc");
        expReq.setSentenceAnalysis("analysis");
        expReq.setSubtitleAt("00:00:10");
        expReq.setVideoId("abc123");

        AddExpressionToBookListRequest request = new AddExpressionToBookListRequest(List.of(expReq));
        Videos video = Videos.builder().id("abc123").build();

        when(expressionBookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(videoRepository.findById("abc123")).thenReturn(Optional.of(video));
        when(expressionRepository.save(any())).thenAnswer(inv -> {
            Expression exp = inv.getArgument(0);
            Field id = Expression.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(exp, 10L);
            return exp;
        });

        service.addExpressionsFromVideo(bookId, request, member);

        verify(expressionBookItemRepository).save(any());
    }

    @Test
    @DisplayName("표현 삭제 - 표현함 주인이 아닌 경우 예외 발생")
    void deleteExpressionsFromBook_forbidden() {
        DeleteExpressionsRequest request = new DeleteExpressionsRequest();
        request.setExpressionBookId(1L);
        request.setMemberId(1L);
        request.setExpressionIds(List.of(100L));

        ExpressionBook book = mockBook(1L, mockMember(2L)); // 다른 사람

        when(expressionBookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThrows(ServiceException.class, () -> service.deleteExpressionsFromBook(request));
    }

    @Test
    @DisplayName("표현 이동 - 본인 소유 표현함일 경우 정상 이동")
    void moveExpressions_success() throws Exception {
        Long memberId = 1L;
        Long sourceId = 10L;
        Long targetId = 20L;
        List<Long> expressionIds = List.of(100L);

        ExpressionBook source = mockBook(sourceId, mockMember(memberId));
        ExpressionBook target = mockBook(targetId, source.getMember());

        MoveExpressionsRequest req = new MoveExpressionsRequest();
        req.setMemberId(memberId);
        req.setSourceExpressionBookId(sourceId);
        req.setTargetExpressionBookId(targetId);
        req.setExpressionIds(expressionIds);

        when(expressionBookRepository.findById(sourceId)).thenReturn(Optional.of(source));
        when(expressionBookRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(expressionBookItemRepository.existsById(any())).thenReturn(false);

        service.moveExpressions(req);

        verify(expressionBookItemRepository).deleteAllById(any());
        verify(expressionBookItemRepository).save(any());
    }

    private Member mockMember(Long idVal) {
        Member member = Member.builder()
                .email("user@example.com")
                .nickname("유저")
                .loginPlatform(LoginPlatform.GOOGLE)
                .language(Language.ENGLISH)
                .build();
        try {
            Field id = Member.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(member, idVal);
        } catch (Exception ignored) {}
        return member;
    }

    private ExpressionBook mockBook(Long idVal, Member member) {
        ExpressionBook book = ExpressionBook.builder()
                .name("book")
                .language(Language.ENGLISH)
                .member(member)
                .build();
        try {
            Field id = ExpressionBook.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(book, idVal);
        } catch (Exception ignored) {}
        return book;
    }

    @Test
    @DisplayName("deleteExpressionsFromBook()는 표현을 성공적으로 삭제한다")
    void testDeleteExpressionsFromBookSuccess() throws Exception {
        // given
        Long memberId = 1L;
        Long expressionBookId = 10L;
        Long expressionId1 = 100L;
        Long expressionId2 = 101L;

        Member member = Member.builder()
                .email("test@example.com")
                .nickname("tester")
                .language(Language.ENGLISH)
                .loginPlatform(LoginPlatform.KAKAO)
                .build();
        Field memberIdField = Member.class.getDeclaredField("id");
        memberIdField.setAccessible(true);
        memberIdField.set(member, memberId);

        ExpressionBook book = ExpressionBook.builder()
                .name("Test Book")
                .language(Language.ENGLISH)
                .member(member)
                .build();
        Field bookIdField = ExpressionBook.class.getDeclaredField("id");
        bookIdField.setAccessible(true);
        bookIdField.set(book, expressionBookId);

        List<Long> expressionIds = List.of(expressionId1, expressionId2);
        DeleteExpressionsRequest request = new DeleteExpressionsRequest(expressionBookId, memberId, expressionIds);

        when(expressionBookRepository.findById(expressionBookId)).thenReturn(Optional.of(book));

        // when
        service.deleteExpressionsFromBook(request);

        // then
        List<ExpressionBookItemId> expectedIds = expressionIds.stream()
                .map(id -> new ExpressionBookItemId(id, expressionBookId))
                .toList();

        verify(expressionBookRepository).findById(expressionBookId);
        verify(expressionBookItemRepository).deleteAllById(expectedIds);
    }
}
