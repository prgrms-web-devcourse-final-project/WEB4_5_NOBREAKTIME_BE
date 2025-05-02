package com.mallang.mallang_backend.domain.sentence.expressionbookItem;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.DeleteExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.MoveExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItemId;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository.ExpressionBookItemRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.service.impl.ExpressionBookItemServiceImpl;
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
    private ExpressionBookItemRepository expressionBookItemRepository;

    @BeforeEach
    void setUp() {
        expressionBookRepository = mock(ExpressionBookRepository.class);
        expressionBookItemRepository = mock(ExpressionBookItemRepository.class);

        service = new ExpressionBookItemServiceImpl(
                expressionBookRepository,
                expressionBookItemRepository
        );
    }

    @Test
    @DisplayName("표현 삭제 - 표현함 주인이 아닌 경우 예외 발생")
    void deleteExpressionsFromBook_forbidden() {
        DeleteExpressionsRequest request = new DeleteExpressionsRequest();
        request.setExpressionBookId(1L);
        Long memberId = 1L;
        request.setExpressionIds(List.of(100L));

        ExpressionBook book = mockBook(1L, mockMember(2L)); // 다른 사람

        when(expressionBookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThrows(ServiceException.class, () -> service.deleteExpressionsFromBook(request, memberId));
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
        req.setSourceExpressionBookId(sourceId);
        req.setTargetExpressionBookId(targetId);
        req.setExpressionIds(expressionIds);

        when(expressionBookRepository.findById(sourceId)).thenReturn(Optional.of(source));
        when(expressionBookRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(expressionBookItemRepository.existsById(any())).thenReturn(false);

        service.moveExpressions(req, memberId);

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
    @DisplayName("표현 삭제 - 표현을 성공적으로 삭제")
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
        DeleteExpressionsRequest request = new DeleteExpressionsRequest();
        request.setExpressionBookId(expressionBookId);
        request.setExpressionIds(expressionIds);

        when(expressionBookRepository.findById(expressionBookId)).thenReturn(Optional.of(book));

        // when
        service.deleteExpressionsFromBook(request, memberId);

        // then
        List<ExpressionBookItemId> expectedIds = expressionIds.stream()
                .map(id -> new ExpressionBookItemId(id, expressionBookId))
                .toList();

        verify(expressionBookRepository).findById(expressionBookId);
        verify(expressionBookItemRepository).deleteAllById(expectedIds);
    }
}
