package com.mallang.mallang_backend.domain.sentence.expressionbook;

import static com.mallang.mallang_backend.global.util.ReflectionTestUtil.setId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.Subscription;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.quiz.expressionquizresult.repository.ExpressionQuizResultRepository;
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

@ExtendWith(MockitoExtension.class)
class ExpressionBookServiceImplTest2 {
	@Mock
	private MemberRepository memberRepository;

	@Mock
	private ExpressionBookRepository expressionBookRepository;

	@Mock
	private ExpressionBookItemRepository expressionBookItemRepository;

	@Mock
	private ExpressionRepository expressionRepository;

	@Mock
	private ExpressionQuizResultRepository expressionQuizResultRepository;

	@InjectMocks
	private ExpressionBookServiceImpl expressionBookService;

	@Test
	@DisplayName("create()는 표현함을 생성하고 저장된 결과를 반환한다 (Standard 이상)")
	void create_shouldReturnSavedExpressionBook() throws Exception {
		// given
		Long memberId = 1L;

		Member member = Member.builder().build();
		Field idField = Member.class.getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(member, memberId);
		member.updateSubscription(Subscription.STANDARD);

		ExpressionBookRequest request = new ExpressionBookRequest("My Book", Language.ENGLISH);

		ExpressionBook saved = ExpressionBook.builder()
			.name("My Book")
			.language(Language.ENGLISH)
			.member(member)
			.build();

		Field bookIdField = ExpressionBook.class.getDeclaredField("id");
		bookIdField.setAccessible(true);
		bookIdField.set(saved, 100L);

		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		given(expressionBookRepository.save(any())).willReturn(saved);

		// when
		ExpressionBookResponse response = expressionBookService.create(request, memberId);

		// then
		assertNotNull(response);
		assertEquals("My Book", response.getName());
		assertEquals(Language.ENGLISH, response.getLanguage());
		assertEquals(memberId, response.getMemberId());
	}

	@Test
	@DisplayName("create()는 존재하지 않는 회원이면 예외를 던진다")
	void create_shouldThrowIfMemberNotFound() {
		Long memberId = 999L;
		ExpressionBookRequest request = new ExpressionBookRequest("Test", Language.JAPANESE);

		given(memberRepository.findById(memberId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> expressionBookService.create(request, memberId))
			.isInstanceOf(ServiceException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
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
		setId(basicMember, memberId);

		basicMember.updateSubscription(Subscription.BASIC);

		ExpressionBookRequest request = new ExpressionBookRequest("Basic Book", Language.ENGLISH);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(basicMember));

		// expect
		ServiceException ex = assertThrows(ServiceException.class,
			() -> expressionBookService.create(request, memberId));

		assertEquals(ErrorCode.NO_EXPRESSIONBOOK_CREATE_PERMISSION, ex.getErrorCode());
		verify(memberRepository).findById(memberId);
		verify(expressionBookRepository, never()).save(any());
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

		expressionBookService.updateName(bookId, memberId, newName);

		assertThat(book.getName()).isEqualTo(newName);
	}

	@Test
	@DisplayName("updateName()은 표현함이 없으면 예외를 던진다")
	void updateName_shouldThrowIfBookNotFound() {
		Long memberId = 1L;
		Long bookId = 404L;

		given(expressionBookRepository.findById(bookId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> expressionBookService.updateName(bookId, memberId, "New Name"))
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

		assertThatThrownBy(() -> expressionBookService.updateName(bookId, otherId, "Hacked"))
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

		expressionBookService.delete(bookId, memberId);

		then(expressionBookRepository).should().delete(book);
	}

	@Test
	@DisplayName("delete()는 표현함이 없으면 예외")
	void delete_shouldThrowIfBookNotFound() {
		Long memberId = 1L, bookId = 999L;

		given(expressionBookRepository.findById(bookId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> expressionBookService.delete(bookId, memberId))
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

		assertThatThrownBy(() -> expressionBookService.delete(bookId, attackerId))
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

		List<ExpressionBookResponse> result = expressionBookService.getByMember(memberId);

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

		List<ExpressionResponse> result = expressionBookService.getExpressionsByBook(bookId, memberId);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getSentence()).isEqualTo("Hello");
	}

	@Test
	@DisplayName("getExpressionsByBook()은 표현함이 없으면 예외")
	void getExpressionsByBook_shouldThrowIfBookNotFound() {
		Long memberId = 1L, bookId = 999L;

		given(expressionBookRepository.findById(bookId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> expressionBookService.getExpressionsByBook(bookId, memberId))
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

		assertThatThrownBy(() -> expressionBookService.getExpressionsByBook(bookId, attackerId))
			.isInstanceOf(ServiceException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.FORBIDDEN_EXPRESSION_BOOK);
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
		expressionBookService.delete(bookId, memberId);

		// then
		verify(expressionBookItemRepository).deleteAllById_ExpressionBookId(bookId);
		verify(expressionBookRepository).delete(book);
	}

	@Test
	@DisplayName("회원 가입 시 기본 표현함이 언어별로 자동 생성된다")
	void createDefaultExpressionBooks_success() throws Exception {
		// given
		Member member = Member.builder()
			.email("test@test.com")
			.nickname("tester")
			.loginPlatform(LoginPlatform.KAKAO)
			.language(Language.ENGLISH)
			.build();

		Field idField = Member.class.getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(member, 1L);

		// when
		List<ExpressionBook> defaults = ExpressionBook.createDefault(member);

		// then
		assertFalse(defaults.isEmpty());
		for (ExpressionBook book : defaults) {
			assertEquals("기본 표현함", book.getName());
			assertEquals(member, book.getMember());
			assertNotEquals(Language.NONE, book.getLanguage());
		}
	}

	@Test
	@DisplayName("기본 표현함 이름으로 수동 생성하려 하면 예외가 발생한다")
	void createExpressionBook_withDefaultName_throwsException() throws Exception {
		// given
		Member member = Member.builder()
			.email("test@test.com")
			.nickname("tester")
			.loginPlatform(LoginPlatform.KAKAO)
			.language(Language.ENGLISH)
			.build();
		member.updateSubscription(Subscription.STANDARD);

		Field idField = Member.class.getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(member, 1L);

		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

		ExpressionBookRequest request = new ExpressionBookRequest("기본 표현함", Language.ENGLISH);

		// expect
		ServiceException ex = assertThrows(ServiceException.class,
			() -> expressionBookService.create(request, 1L));

		assertEquals(ErrorCode.EXPRESSIONBOOK_CREATE_DEFAULT_FORBIDDEN, ex.getErrorCode());
	}

	@Test
	@DisplayName("기본 표현함은 삭제할 수 없다")
	void deleteDefaultExpressionBook_shouldThrowException() throws Exception {
		// given
		Long memberId = 1L;
		Long bookId = 100L;

		Member member = Member.builder()
			.email("test@test.com")
			.nickname("tester")
			.loginPlatform(LoginPlatform.KAKAO)
			.language(Language.ENGLISH)
			.build();

		Field idField = Member.class.getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(member, memberId);

		ExpressionBook defaultBook = ExpressionBook.builder()
			.name("기본 표현함")
			.language(Language.ENGLISH)
			.member(member)
			.build();

		Field bookIdField = ExpressionBook.class.getDeclaredField("id");
		bookIdField.setAccessible(true);
		bookIdField.set(defaultBook, bookId);

		when(expressionBookRepository.findById(bookId)).thenReturn(Optional.of(defaultBook));

		// expect
		ServiceException ex = assertThrows(ServiceException.class,
			() -> expressionBookService.delete(bookId, memberId));

		assertEquals(ErrorCode.EXPRESSIONBOOK_DELETE_DEFAULT_FORBIDDEN, ex.getErrorCode());
	}

	@Test
	@DisplayName("동일한 이름의 표현함이 이미 존재하면 예외가 발생한다")
	void createExpressionBook_withDuplicateName_shouldThrowException() throws Exception {
		// given
		Long memberId = 1L;
		String duplicateName = "MyBook";

		Member mockMember = Member.builder()
			.email("test@user.com")
			.password("pw")
			.nickname("tester")
			.profileImageUrl(null)
			.loginPlatform(LoginPlatform.KAKAO)
			.language(Language.ENGLISH)
			.build();
		mockMember.updateSubscription(Subscription.STANDARD);

		Field idField = Member.class.getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(mockMember, memberId);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
		when(expressionBookRepository.existsByMemberAndName(mockMember, duplicateName)).thenReturn(true);

		ExpressionBookRequest request = new ExpressionBookRequest(duplicateName, Language.ENGLISH);

		// when & then
		ServiceException exception = assertThrows(ServiceException.class, () -> {
			expressionBookService.create(request, memberId);
		});

		assertEquals(ErrorCode.DUPLICATE_EXPRESSIONBOOK_NAME, exception.getErrorCode());
		verify(expressionBookRepository, never()).save(any());
	}

	@Test
	@DisplayName("성공 - 특정 회원의 표현책에서 키워드가 포함된 표현 검색")
	void searchExpressions_success() {
		// given
		Long memberId = 1L;
		String keyword = "hello";

		Expression expression1 = Expression.builder()
			.sentence("hello world")
			.subtitleAt(LocalTime.of(0,0, 8))
			.build();
		setId(expression1, 1L);

		Expression expression2 = Expression.builder()
			.sentence("say hello to my little friend")
			.subtitleAt(LocalTime.of(0, 0, 10))
			.build();
		setId(expression2, 2L);

		given(expressionBookItemRepository.findExpressionsByMemberAndKeyword(memberId, keyword))
			.willReturn(List.of(expression1, expression2));

		// when
		List<ExpressionResponse> responses = expressionBookService.searchExpressions(memberId, keyword);

		// then
		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).getSentence()).isEqualTo("hello world");
		assertThat(responses.get(1).getSentence()).isEqualTo("say hello to my little friend");
		verify(expressionBookItemRepository).findExpressionsByMemberAndKeyword(memberId, keyword);
	}
}
