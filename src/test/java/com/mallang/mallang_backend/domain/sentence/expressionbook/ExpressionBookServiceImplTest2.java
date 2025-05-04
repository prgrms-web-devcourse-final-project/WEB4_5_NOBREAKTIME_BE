package com.mallang.mallang_backend.domain.sentence.expressionbook;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.Subscription;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpressionBookServiceImplTest2 {

    private MemberRepository memberRepository;
    private ExpressionBookRepository expressionBookRepository;
    private ExpressionBookItemRepository expressionBookItemRepository;
    private ExpressionRepository expressionRepository;

    private ExpressionBookServiceImpl service;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        expressionBookRepository = mock(ExpressionBookRepository.class);
        expressionBookItemRepository = mock(ExpressionBookItemRepository.class);
        expressionRepository = mock(ExpressionRepository.class);

        service = new ExpressionBookServiceImpl(
                expressionBookRepository,
                memberRepository,
                expressionBookItemRepository,
                expressionRepository,
                null,
                null
        );
    }

    @Test
    @DisplayName("create()는 표현함을 생성하고 저장된 결과를 반환한다(Standard 이상)")
    void testCreateExpressionBook() throws Exception {
        // given
        Long memberId = 1L;

        Member mockMember = Member.builder().build();
        Field idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(mockMember, memberId);
        mockMember.updateSubscription(Subscription.STANDARD);

        ExpressionBookRequest request = new ExpressionBookRequest("My Book", Language.ENGLISH);

        ExpressionBook savedBook = ExpressionBook.builder()
                .name("My Book")
                .language(Language.ENGLISH)
                .member(mockMember)
                .build();

        Field bookIdField = ExpressionBook.class.getDeclaredField("id");
        bookIdField.setAccessible(true);
        bookIdField.set(savedBook, 100L);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(expressionBookRepository.save(any(ExpressionBook.class))).thenReturn(savedBook);

        // when
        ExpressionBookResponse response = service.create(request, memberId);

        // then
        assertNotNull(response);
        assertEquals("My Book", response.getName());
        assertEquals(Language.ENGLISH, response.getLanguage());
        assertEquals(memberId, response.getMemberId());
    }


    @Test
    @DisplayName("create()는 존재하지 않는 회원이면 예외를 던진다")
    void testCreateWithInvalidMember() {
        // given
        Long memberId = 999L;
        ExpressionBookRequest request = new ExpressionBookRequest("Test", Language.JAPANESE);

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // expect
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.create(request, memberId));

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        verify(memberRepository).findById(memberId);
        verify(expressionBookRepository, never()).save(any());
    }

    @Test
    @DisplayName("create()는 BASIC 회원이 생성 시 예외를 던진다")
    void testCreateExpressionBook_failsForBasicUser() throws Exception {
        // given
        Long memberId = 1L;

        Member basicMember = Member.builder()
                .email("basic@test.com")
                .password("pw")
                .nickname("basic")
                .profileImageUrl(null)
                .loginPlatform(LoginPlatform.KAKAO)
                .language(Language.ENGLISH)
                .build();

        basicMember.updateSubscription(Subscription.BASIC);

        Field idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(basicMember, memberId);

        ExpressionBookRequest request = new ExpressionBookRequest("Basic Book", Language.ENGLISH);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(basicMember));

        // expect
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.create(request, memberId));

        assertEquals(ErrorCode.NO_EXPRESSIONBOOK_CREATE_PERMISSION, ex.getErrorCode());
        verify(memberRepository).findById(memberId);
        verify(expressionBookRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateName()은 표현함 이름을 정상적으로 수정한다")
    void testUpdateNameSuccess() throws Exception {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        String newName = "Updated Name";

        Member mockMember = Member.builder()
                .email("test@email.com")
                .password("pw")
                .nickname("tester")
                .profileImageUrl(null)
                .loginPlatform(LoginPlatform.KAKAO)
                .language(Language.ENGLISH)
                .build();

        Field memberIdField = Member.class.getDeclaredField("id");
        memberIdField.setAccessible(true);
        memberIdField.set(mockMember, memberId);

        ExpressionBook book = ExpressionBook.builder()
                .name("Old Name")
                .language(Language.ENGLISH)
                .member(mockMember)
                .build();

        Field bookIdField = ExpressionBook.class.getDeclaredField("id");
        bookIdField.setAccessible(true);
        bookIdField.set(book, bookId);

        when(expressionBookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // when
        service.updateName(bookId, memberId, newName);

        // then
        assertEquals(newName, book.getName());
        verify(expressionBookRepository).findById(bookId);
    }

    @Test
    @DisplayName("updateName()은 표현함이 존재하지 않으면 예외를 던진다")
    void testUpdateName_BookNotFound() {
        // given
        Long bookId = 404L;
        Long memberId = 1L;

        when(expressionBookRepository.findById(bookId)).thenReturn(Optional.empty());

        // expect
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateName(bookId, memberId, "New Name"));

        assertEquals(ErrorCode.EXPRESSION_BOOK_NOT_FOUND, ex.getErrorCode());
        verify(expressionBookRepository).findById(bookId);
    }

    @Test
    @DisplayName("updateName()은 다른 사용자가 접근하면 예외를 던진다")
    void testUpdateName_ForbiddenAccess() throws Exception {
        // given
        Long bookId = 10L;
        Long ownerId = 1L;
        Long otherUserId = 2L;

        Member owner = Member.builder()
                .email("owner@test.com")
                .password("1234")
                .nickname("owner")
                .loginPlatform(LoginPlatform.KAKAO)
                .language(Language.ENGLISH)
                .build();
        Field idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(owner, ownerId);

        ExpressionBook book = ExpressionBook.builder()
                .name("My Book")
                .language(Language.ENGLISH)
                .member(owner)
                .build();
        Field bookIdField = ExpressionBook.class.getDeclaredField("id");
        bookIdField.setAccessible(true);
        bookIdField.set(book, bookId);

        when(expressionBookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // expect
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateName(bookId, otherUserId, "Hacked"));

        assertEquals(ErrorCode.FORBIDDEN_EXPRESSION_BOOK, ex.getErrorCode());
        verify(expressionBookRepository).findById(bookId);
    }

    @Test
    @DisplayName("delete()는 본인의 표현함이면 정상 삭제한다")
    void testDeleteExpressionBookSuccess() throws Exception {
        // given
        Long memberId = 1L;
        Long bookId = 10L;

        Member mockMember = Member.builder()
                .email("user@test.com")
                .password("pass")
                .nickname("user")
                .profileImageUrl(null)
                .loginPlatform(LoginPlatform.KAKAO)
                .language(Language.ENGLISH)
                .build();

        Field memberIdField = Member.class.getDeclaredField("id");
        memberIdField.setAccessible(true);
        memberIdField.set(mockMember, memberId);

        ExpressionBook book = ExpressionBook.builder()
                .name("My Book")
                .language(Language.ENGLISH)
                .member(mockMember)
                .build();

        Field bookIdField = ExpressionBook.class.getDeclaredField("id");
        bookIdField.setAccessible(true);
        bookIdField.set(book, bookId);

        when(expressionBookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // when
        service.delete(bookId, memberId);

        // then
        verify(expressionBookRepository).findById(bookId);
        verify(expressionBookRepository).delete(book);
    }

    @Test
    @DisplayName("delete()는 존재하지 않는 표현함이면 예외를 던진다")
    void testDeleteExpressionBookNotFound() {
        // given
        Long memberId = 1L;
        Long bookId = 999L;

        when(expressionBookRepository.findById(bookId)).thenReturn(Optional.empty());

        // expect
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.delete(bookId, memberId));

        assertEquals(ErrorCode.EXPRESSION_BOOK_NOT_FOUND, ex.getErrorCode());
        verify(expressionBookRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete()는 본인 소유가 아닌 표현함이면 예외를 던진다")
    void testDeleteExpressionBookForbidden() throws Exception {
        // given
        Long realOwnerId = 1L;
        Long attackerId = 2L;
        Long bookId = 10L;

        Member realOwner = Member.builder()
                .email("owner@test.com")
                .password("pass")
                .nickname("owner")
                .profileImageUrl(null)
                .loginPlatform(LoginPlatform.KAKAO)
                .language(Language.ENGLISH)
                .build();

        Field idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(realOwner, realOwnerId);

        ExpressionBook book = ExpressionBook.builder()
                .name("Not Yours")
                .language(Language.ENGLISH)
                .member(realOwner)
                .build();

        Field bookIdField = ExpressionBook.class.getDeclaredField("id");
        bookIdField.setAccessible(true);
        bookIdField.set(book, bookId);

        when(expressionBookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // expect
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.delete(bookId, attackerId));

        assertEquals(ErrorCode.FORBIDDEN_EXPRESSION_BOOK, ex.getErrorCode());
        verify(expressionBookRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getByMember()는 사용자의 표현함 리스트를 반환한다")
    void testGetByMemberSuccess() throws Exception {
        // given
        Long memberId = 1L;

        Member mockMember = Member.builder()
                .email("test@user.com")
                .password("pw")
                .nickname("tester")
                .profileImageUrl(null)
                .loginPlatform(LoginPlatform.KAKAO)
                .language(Language.ENGLISH)
                .build();

        Field idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(mockMember, memberId);

        ExpressionBook book1 = ExpressionBook.builder()
                .name("Book One")
                .language(Language.ENGLISH)
                .member(mockMember)
                .build();

        ExpressionBook book2 = ExpressionBook.builder()
                .name("Book Two")
                .language(Language.ENGLISH)
                .member(mockMember)
                .build();

        Field id1 = ExpressionBook.class.getDeclaredField("id");
        id1.setAccessible(true);
        id1.set(book1, 101L);

        Field id2 = ExpressionBook.class.getDeclaredField("id");
        id2.setAccessible(true);
        id2.set(book2, 102L);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(expressionBookRepository.findAllByMember(mockMember)).thenReturn(List.of(book1, book2));

        // when
        List<ExpressionBookResponse> responses = service.getByMember(memberId);

        // then
        assertEquals(2, responses.size());
        assertEquals("Book One", responses.get(0).getName());
        assertEquals("Book Two", responses.get(1).getName());

        verify(memberRepository).findById(memberId);
        verify(expressionBookRepository).findAllByMember(mockMember);
    }

    @Test
    @DisplayName("getExpressionsByBook()은 표현함 ID와 사용자 ID로 표현 리스트를 반환한다")
    void testGetExpressionsByBookSuccess() throws Exception {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        Long expressionId = 100L;

        Member member = Member.builder()
                .email("user@test.com")
                .password("pw")
                .nickname("user")
                .profileImageUrl(null)
                .loginPlatform(LoginPlatform.KAKAO)
                .language(Language.ENGLISH)
                .build();
        Field memberIdField = Member.class.getDeclaredField("id");
        memberIdField.setAccessible(true);
        memberIdField.set(member, memberId);

        ExpressionBook book = ExpressionBook.builder()
                .name("My Book")
                .language(Language.ENGLISH)
                .member(member)
                .build();
        Field bookIdField = ExpressionBook.class.getDeclaredField("id");
        bookIdField.setAccessible(true);
        bookIdField.set(book, bookId);

        ExpressionBookItem item = ExpressionBookItem.builder()
                .expressionId(expressionId)
                .expressionBookId(bookId)
                .build();

        Expression expression = Expression.builder()
                .sentence("Hello, world!")
                .description("인삿말")
                .sentenceAnalysis("Hello (감탄사), world (명사)")
                .subtitleAt(LocalTime.of(0, 1, 5))
                .videos(null)
                .build();
        Field expIdField = Expression.class.getDeclaredField("id");
        expIdField.setAccessible(true);
        expIdField.set(expression, expressionId);

        when(expressionBookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(expressionBookItemRepository.findAllById_ExpressionBookId(bookId)).thenReturn(List.of(item));
        when(expressionRepository.findById(expressionId)).thenReturn(Optional.of(expression));

        // when
        List<ExpressionResponse> responses = service.getExpressionsByBook(bookId, memberId);

        // then
        assertEquals(1, responses.size());
        assertEquals("Hello, world!", responses.get(0).getSentence());
    }

    @Test
    @DisplayName("getExpressionsByBook()은 표현함이 없으면 예외를 던진다")
    void testGetExpressionsByBook_NotFound() {
        Long memberId = 1L;
        Long bookId = 999L;

        when(expressionBookRepository.findById(bookId)).thenReturn(Optional.empty());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.getExpressionsByBook(bookId, memberId));

        assertEquals(ErrorCode.EXPRESSION_BOOK_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("getExpressionsByBook()은 표현함 소유자가 아니면 예외를 던진다")
    void testGetExpressionsByBook_Forbidden() throws Exception {
        Long bookId = 10L;
        Long memberId = 1L;
        Long attackerId = 2L;

        Member owner = Member.builder()
                .email("owner@test.com")
                .password("pw")
                .nickname("owner")
                .profileImageUrl(null)
                .loginPlatform(LoginPlatform.KAKAO)
                .language(Language.ENGLISH)
                .build();
        Field idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(owner, memberId);

        ExpressionBook book = ExpressionBook.builder()
                .name("Private Book")
                .language(Language.ENGLISH)
                .member(owner)
                .build();
        Field bookIdField = ExpressionBook.class.getDeclaredField("id");
        bookIdField.setAccessible(true);
        bookIdField.set(book, bookId);

        when(expressionBookRepository.findById(bookId)).thenReturn(Optional.of(book));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.getExpressionsByBook(bookId, attackerId));

        assertEquals(ErrorCode.FORBIDDEN_EXPRESSION_BOOK, ex.getErrorCode());
    }

    @Test
    @DisplayName("delete()는 표현함과 그 안의 표현 아이템들을 모두 삭제한다")
    void deleteExpressionBook_andItsItems() throws Exception {
        // given
        Long memberId = 1L;
        Long bookId = 10L;

        Member member = Member.builder()
                .email("user@test.com")
                .password("pw")
                .nickname("user")
                .loginPlatform(LoginPlatform.KAKAO)
                .language(Language.ENGLISH)
                .build();
        Field memberIdField = Member.class.getDeclaredField("id");
        memberIdField.setAccessible(true);
        memberIdField.set(member, memberId);

        ExpressionBook book = ExpressionBook.builder()
                .name("My Book")
                .language(Language.ENGLISH)
                .member(member)
                .build();
        Field bookIdField = ExpressionBook.class.getDeclaredField("id");
        bookIdField.setAccessible(true);
        bookIdField.set(book, bookId);

        when(expressionBookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // when
        service.delete(bookId, memberId);

        // then
        verify(expressionBookItemRepository).deleteAllById_ExpressionBookId(bookId);
        verify(expressionBookRepository).delete(book);
    }

}
