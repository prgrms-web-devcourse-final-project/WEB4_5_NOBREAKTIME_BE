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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpressionBookItemServiceImplTest {

    private ExpressionBookItemServiceImpl service;
    private ExpressionBookRepository expressionBookRepository;
    private ExpressionBookItemRepository expressionBookItemRepository;

    @BeforeEach
    void setUp() {
        expressionBookRepository = mock(ExpressionBookRepository.class);
        expressionBookItemRepository = mock(ExpressionBookItemRepository.class);
        service = new ExpressionBookItemServiceImpl(expressionBookRepository, expressionBookItemRepository);
    }

    @Test
    @DisplayName("deleteExpressionsFromBook(): 표현 삭제 성공")
    void deleteExpressionsFromBook_shouldDeleteSuccessfully() throws Exception {
        Long memberId = 1L;
        Long bookId = 10L;
        List<Long> expressionIds = List.of(100L, 101L);

        Member member = mockMember(memberId);
        ExpressionBook book = mockBook(bookId, member);

        DeleteExpressionsRequest request = new DeleteExpressionsRequest();
        request.setExpressionBookId(bookId);
        request.setExpressionIds(expressionIds);

        when(expressionBookRepository.findById(bookId)).thenReturn(Optional.of(book));

        service.deleteExpressionsFromBook(request, memberId);

        List<ExpressionBookItemId> expectedIds = expressionIds.stream()
                .map(id -> new ExpressionBookItemId(id, bookId))
                .toList();

        verify(expressionBookRepository).findById(bookId);
        verify(expressionBookItemRepository).deleteAllById(expectedIds);
    }

    @Test
    @DisplayName("deleteExpressionsFromBook(): 표현함 소유자가 아니면 예외 발생")
    void deleteExpressionsFromBook_shouldThrowExceptionWhenNotOwner() {
        Long memberId = 1L;
        ExpressionBook book = mockBook(1L, mockMember(999L)); // 다른 사람

        DeleteExpressionsRequest request = new DeleteExpressionsRequest();
        request.setExpressionBookId(1L);
        request.setExpressionIds(List.of(100L));

        when(expressionBookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThrows(ServiceException.class,
                () -> service.deleteExpressionsFromBook(request, memberId));
    }

    @Test
    @DisplayName("moveExpressions(): 표현 이동 성공")
    void moveExpressions_shouldMoveSuccessfully() throws Exception {
        Long memberId = 1L;
        Long sourceId = 10L;
        Long targetId = 20L;
        List<Long> expressionIds = List.of(100L);

        Member member = mockMember(memberId);
        ExpressionBook source = mockBook(sourceId, member);
        ExpressionBook target = mockBook(targetId, member);

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

    @Test
    @DisplayName("moveExpressions(): source 표현함이 없으면 예외 발생")
    void moveExpressions_shouldThrowWhenSourceNotFound() {
        MoveExpressionsRequest request = new MoveExpressionsRequest();
        request.setSourceExpressionBookId(1L);
        request.setTargetExpressionBookId(2L);
        request.setExpressionIds(List.of(100L));

        when(expressionBookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class,
                () -> service.moveExpressions(request, 1L));
    }

    @Test
    @DisplayName("moveExpressions(): target 표현함이 없으면 예외 발생")
    void moveExpressions_shouldThrowWhenTargetNotFound() {
        Long memberId = 1L;
        ExpressionBook source = mockBook(1L, mockMember(memberId));

        MoveExpressionsRequest request = new MoveExpressionsRequest();
        request.setSourceExpressionBookId(1L);
        request.setTargetExpressionBookId(2L);
        request.setExpressionIds(List.of(100L));

        when(expressionBookRepository.findById(1L)).thenReturn(Optional.of(source));
        when(expressionBookRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class,
                () -> service.moveExpressions(request, memberId));
    }

    @Test
    @DisplayName("moveExpressions(): source 표현함 소유자가 아니면 예외 발생")
    void moveExpressions_shouldThrowWhenNotOwnerOfSource() {
        Long memberId = 1L;
        Member other = mockMember(2L);

        ExpressionBook source = mockBook(1L, other);
        ExpressionBook target = mockBook(2L, other);

        MoveExpressionsRequest request = new MoveExpressionsRequest();
        request.setSourceExpressionBookId(1L);
        request.setTargetExpressionBookId(2L);
        request.setExpressionIds(List.of(100L));

        when(expressionBookRepository.findById(1L)).thenReturn(Optional.of(source));
        when(expressionBookRepository.findById(2L)).thenReturn(Optional.of(target));

        assertThrows(ServiceException.class,
                () -> service.moveExpressions(request, memberId));
    }

    // -------------------- 공통  --------------------

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
}
