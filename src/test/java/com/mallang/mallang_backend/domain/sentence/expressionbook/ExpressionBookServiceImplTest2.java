package com.mallang.mallang_backend.domain.sentence.expressionbook;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookResponse;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionResponse;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.service.impl.ExpressionBookServiceImpl;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository.ExpressionBookItemRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ExpressionBookServiceImplTest2 {

    @Mock private MemberRepository memberRepository;
    @Mock private ExpressionBookRepository expressionBookRepository;
    @Mock private ExpressionBookItemRepository expressionBookItemRepository;
    @Mock private ExpressionRepository expressionRepository;
    @InjectMocks private ExpressionBookServiceImpl service;

    @Test
    @DisplayName("create()는 표현함을 생성하고 저장된 결과를 반환한다")
    void create_shouldReturnSavedExpressionBook() {
        Long memberId = 1L;
        Member member = Member.builder().build();
        ReflectionTestUtils.setField(member, "id", memberId);

        ExpressionBookRequest request = new ExpressionBookRequest("My Book", Language.ENGLISH);
        ExpressionBook saved = ExpressionBook.builder().name("My Book").language(Language.ENGLISH).member(member).build();
        ReflectionTestUtils.setField(saved, "id", 100L);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(expressionBookRepository.save(any())).willReturn(saved);

        ExpressionBookResponse response = service.create(request, memberId);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("My Book");
        assertThat(response.getLanguage()).isEqualTo(Language.ENGLISH);
        assertThat(response.getMemberId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("create()는 존재하지 않는 회원이면 예외를 던진다")
    void create_shouldThrowIfMemberNotFound() {
        Long memberId = 999L;
        ExpressionBookRequest request = new ExpressionBookRequest("Test", Language.JAPANESE);

        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request, memberId))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("updateName()은 표현함 이름을 수정한다")
    void updateName_shouldUpdateExpressionBookName() {
        Long memberId = 1L;
        Long bookId = 10L;
        String newName = "Updated Name";

        Member member = Member.builder().language(Language.ENGLISH).build();
        ReflectionTestUtils.setField(member, "id", memberId);

        ExpressionBook book = ExpressionBook.builder().name("Old Name").language(Language.ENGLISH).member(member).build();
        ReflectionTestUtils.setField(book, "id", bookId);

        given(expressionBookRepository.findById(bookId)).willReturn(Optional.of(book));

        service.updateName(bookId, memberId, newName);

        assertThat(book.getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("updateName()은 표현함이 없으면 예외를 던진다")
    void updateName_shouldThrowIfBookNotFound() {
        Long memberId = 1L;
        Long bookId = 404L;

        given(expressionBookRepository.findById(bookId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateName(bookId, memberId, "New Name"))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXPRESSION_BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("updateName()은 본인이 아닌 경우 예외를 던진다")
    void updateName_shouldThrowIfAccessForbidden() {
        Long ownerId = 1L, otherId = 2L, bookId = 10L;

        Member owner = Member.builder().language(Language.ENGLISH).build();
        ReflectionTestUtils.setField(owner, "id", ownerId);

        ExpressionBook book = ExpressionBook.builder().name("Book").language(Language.ENGLISH).member(owner).build();
        ReflectionTestUtils.setField(book, "id", bookId);

        given(expressionBookRepository.findById(bookId)).willReturn(Optional.of(book));

        assertThatThrownBy(() -> service.updateName(bookId, otherId, "Hacked"))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN_EXPRESSION_BOOK);
    }

    @Test
    @DisplayName("delete()는 본인이 소유한 표현함을 삭제한다")
    void delete_shouldRemoveExpressionBookIfOwner() {
        Long memberId = 1L, bookId = 10L;

        Member member = Member.builder().language(Language.ENGLISH).build();
        ReflectionTestUtils.setField(member, "id", memberId);

        ExpressionBook book = ExpressionBook.builder().name("My Book").language(Language.ENGLISH).member(member).build();
        ReflectionTestUtils.setField(book, "id", bookId);

        given(expressionBookRepository.findById(bookId)).willReturn(Optional.of(book));

        service.delete(bookId, memberId);

        then(expressionBookRepository).should().delete(book);
    }

    @Test
    @DisplayName("delete()는 표현함이 없으면 예외")
    void delete_shouldThrowIfBookNotFound() {
        Long memberId = 1L, bookId = 999L;

        given(expressionBookRepository.findById(bookId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(bookId, memberId))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXPRESSION_BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("delete()는 다른 사용자의 표현함이면 예외")
    void delete_shouldThrowIfNotOwner() {
        Long ownerId = 1L, attackerId = 2L, bookId = 10L;

        Member owner = Member.builder().language(Language.ENGLISH).build();
        ReflectionTestUtils.setField(owner, "id", ownerId);

        ExpressionBook book = ExpressionBook.builder().name("Not Yours").language(Language.ENGLISH).member(owner).build();
        ReflectionTestUtils.setField(book, "id", bookId);

        given(expressionBookRepository.findById(bookId)).willReturn(Optional.of(book));

        assertThatThrownBy(() -> service.delete(bookId, attackerId))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN_EXPRESSION_BOOK);
    }

    @Test
    @DisplayName("getByMember()는 사용자의 표현함 목록을 반환한다")
    void getByMember_shouldReturnListOfBooks() {
        Long memberId = 1L;
        Member member = Member.builder().language(Language.ENGLISH).build();
        ReflectionTestUtils.setField(member, "id", memberId);

        ExpressionBook book1 = ExpressionBook.builder().name("One").language(Language.ENGLISH).member(member).build();
        ExpressionBook book2 = ExpressionBook.builder().name("Two").language(Language.ENGLISH).member(member).build();
        ReflectionTestUtils.setField(book1, "id", 101L);
        ReflectionTestUtils.setField(book2, "id", 102L);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(expressionBookRepository.findAllByMember(member)).willReturn(List.of(book1, book2));

        List<ExpressionBookResponse> result = service.getByMember(memberId);

        assertThat(result).hasSize(2)
                .extracting(ExpressionBookResponse::getName)
                .containsExactly("One", "Two");
    }

    @Test
    @DisplayName("getExpressionsByBook()은 표현 리스트를 반환한다")
    void getExpressionsByBook_shouldReturnExpressions() {
        Long memberId = 1L, bookId = 10L, expressionId = 100L;

        Member member = Member.builder().language(Language.ENGLISH).build();
        ReflectionTestUtils.setField(member, "id", memberId);

        ExpressionBook book = ExpressionBook.builder().name("My Book").language(Language.ENGLISH).member(member).build();
        ReflectionTestUtils.setField(book, "id", bookId);

        ExpressionBookItem item = ExpressionBookItem.builder().expressionBookId(bookId).expressionId(expressionId).build();
        Expression expression = Expression.builder().sentence("Hello").description("desc").sentenceAnalysis("anl").subtitleAt(LocalTime.of(0, 1, 5)).videos(null).build();
        ReflectionTestUtils.setField(expression, "id", expressionId);

        given(expressionBookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(expressionBookItemRepository.findAllById_ExpressionBookId(bookId)).willReturn(List.of(item));
        given(expressionRepository.findById(expressionId)).willReturn(Optional.of(expression));

        List<ExpressionResponse> result = service.getExpressionsByBook(bookId, memberId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSentence()).isEqualTo("Hello");
    }

    @Test
    @DisplayName("getExpressionsByBook()은 표현함이 없으면 예외")
    void getExpressionsByBook_shouldThrowIfBookNotFound() {
        Long memberId = 1L, bookId = 999L;

        given(expressionBookRepository.findById(bookId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getExpressionsByBook(bookId, memberId))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXPRESSION_BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("getExpressionsByBook()은 본인 소유가 아니면 예외")
    void getExpressionsByBook_shouldThrowIfNotOwner() {
        Long ownerId = 1L, attackerId = 2L, bookId = 10L;

        Member owner = Member.builder().language(Language.ENGLISH).build();
        ReflectionTestUtils.setField(owner, "id", ownerId);

        ExpressionBook book = ExpressionBook.builder().name("Private").language(Language.ENGLISH).member(owner).build();
        ReflectionTestUtils.setField(book, "id", bookId);

        given(expressionBookRepository.findById(bookId)).willReturn(Optional.of(book));

        assertThatThrownBy(() -> service.getExpressionsByBook(bookId, attackerId))
                .isInstanceOf(ServiceException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN_EXPRESSION_BOOK);
    }
}
