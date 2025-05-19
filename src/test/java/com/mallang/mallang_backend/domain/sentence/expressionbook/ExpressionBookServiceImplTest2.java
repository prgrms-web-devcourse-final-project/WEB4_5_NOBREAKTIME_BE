package com.mallang.mallang_backend.domain.sentence.expressionbook;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
		member.updateLearningLanguage(Language.ENGLISH);
		member.updateSubscription(SubscriptionType.STANDARD);

		ExpressionBookRequest request = new ExpressionBookRequest("My Book");

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
		Long response = expressionBookService.create(request, memberId);

		// then
		assertNotNull(response);
		assertEquals(response, saved.getId());
	}

	@Test
	@DisplayName("create()는 존재하지 않는 회원이면 예외를 던진다")
	void create_shouldThrowIfMemberNotFound() {
		Long memberId = 999L;
		ExpressionBookRequest request = new ExpressionBookRequest("Test");

		given(memberRepository.findById(memberId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> expressionBookService.create(request, memberId))
			.isInstanceOf(ServiceException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
	}

	@Test
	@DisplayName("create()는 BASIC 회원이 생성 시 예외를 던진다")
	void testCreateExpressionBook_failsForBasicUser() {
		// given
		Long memberId = 1L;

		Member basicMember = Member.builder()
			.email("basic@test.com")
			.nickname("basic")
			.profileImageUrl(null)
			.loginPlatform(LoginPlatform.KAKAO)
			.language(Language.ENGLISH)
			.build();
		setId(basicMember, memberId);

		basicMember.updateSubscription(SubscriptionType.BASIC);

		ExpressionBookRequest request = new ExpressionBookRequest("Basic Book");

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
		member.updateSubscription(SubscriptionType.STANDARD.STANDARD);
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
		member.updateSubscription(SubscriptionType.STANDARD);
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
		member.updateSubscription(SubscriptionType.STANDARD);
		ReflectionTestUtils.setField(member, "id", memberId);

		ExpressionBook book1 = ExpressionBook.builder().name("One").language(Language.ENGLISH).member(member).build();
		ExpressionBook book2 = ExpressionBook.builder().name("Two").language(Language.ENGLISH).member(member).build();
		ReflectionTestUtils.setField(book1, "id", 101L);
		ReflectionTestUtils.setField(book2, "id", 102L);

		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		given(expressionBookRepository.findAllByMemberIdAndLanguage(memberId, member.getLanguage())).willReturn(List.of(book1, book2));

		List<ExpressionBookResponse> result = expressionBookService.getByMember(memberId);

		assertThat(result).hasSize(2)
			.extracting(ExpressionBookResponse::getName)
			.containsExactly("One", "Two");
	}

	@Test
	@DisplayName("getExpressionsByBook(): 표현함 ID 리스트에 해당하는 표현들을 최신순으로 반환한다")
	void getExpressionsByBook_ExpressionResponsesInSortedOrder() {
		// given
		Long memberId = 1L;
		Member member = Member.builder()
                .language(Language.ENGLISH)
                .build();
		setId(member, memberId);

		Long bookId1 = 10L;
		Long bookId2 = 11L;

		ExpressionBook book1 = ExpressionBook.builder()
                .name("A")
                .language(Language.ENGLISH)
                .member(member)
                .build();

		ExpressionBook book2 = ExpressionBook.builder()
                .name("B")
                .language(Language.ENGLISH)
                .member(member)
                .build();

		setId(book1, bookId1);
		setId(book2, bookId2);

		// 표현 아이템 2개 (createdAt 기준으로 최신 먼저)
		ExpressionBookItem item1 = ExpressionBookItem.builder()
				.expressionBookId(bookId1)
				.expressionId(101L)
				.build();

		ExpressionBookItem item2 = ExpressionBookItem.builder()
				.expressionBookId(bookId2)
				.expressionId(102L)
				.build();

		setId(item1, "createdAt", LocalDateTime.of(2025, 5, 16, 10, 0));
		setId(item2, "createdAt", LocalDateTime.of(2025, 5, 16, 11, 0)); // 최신

		Expression expression1 = Expression.builder()
				.sentence("Sentence 1")
				.description("desc1")
				.sentenceAnalysis("analysis1")
				.subtitleAt(LocalTime.of(0, 0, 5))
				.build();

		Expression expression2 = Expression.builder()
				.sentence("Sentence 2")
				.description("desc2")
				.sentenceAnalysis("analysis2")
				.subtitleAt(LocalTime.of(0, 0, 10))
				.build();

		setId(expression1, 101L);
		setId(expression2, 102L);

		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		given(expressionBookRepository.findAllById(List.of(bookId1, bookId2))).willReturn(List.of(book1, book2));
		given(expressionBookItemRepository.findAllById_ExpressionBookIdInOrderByCreatedAtDesc(List.of(bookId1, bookId2)))
				.willReturn(List.of(item2, item1)); // 최신순
		given(expressionRepository.findAllById(List.of(102L, 101L))).willReturn(List.of(expression2, expression1));

		// when
		List<ExpressionResponse> result = expressionBookService.getExpressionsByBook(List.of(bookId1, bookId2), memberId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getExpressionId()).isEqualTo(102L); // 최신 표현 먼저
		assertThat(result.get(1).getExpressionId()).isEqualTo(101L);
		assertThat(result.get(0).getSentence()).isEqualTo("Sentence 2");
		assertThat(result.get(1).getSentence()).isEqualTo("Sentence 1");
	}

	@Test
	@DisplayName("getExpressionsByBook(): 존재하지 않는 표현함 ID가 포함된 경우 예외를 던진다")
	void getExpressionsByBook_shouldThrow_whenExpressionBookIdNotExist() {
		// given
		Long memberId = 1L;
		List<Long> requestIds = List.of(100L, 999L); // 999는 존재하지 않음

		Member member = Member.builder()
                .language(Language.ENGLISH)
                .build();
		setId(member, memberId);

		ExpressionBook validBook = ExpressionBook.builder()
                .name("Valid")
                .language(Language.ENGLISH)
                .member(member)
                .build();

		setId(validBook, 100L);

		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		given(expressionBookRepository.findAllById(requestIds)).willReturn(List.of(validBook)); // 999 없음

		// when & then
		ServiceException ex = assertThrows(ServiceException.class, () ->
				expressionBookService.getExpressionsByBook(requestIds, memberId)
		);

		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.EXPRESSION_BOOK_NOT_FOUND);
	}

	@Test
	@DisplayName("getExpressionsByBook(): 다른 사용자의 표현함이 포함된 경우 예외를 던진다")
	void getExpressionsByBook_whenExpressionBookOwnedByAnotherUser() {
		// given
		Long memberId = 1L;
		Long otherId = 2L;
		Long bookId = 10L;

		Member user = Member.builder()
                .language(Language.ENGLISH)
                .build();

		Member other = Member
                .builder()
                .language(Language.ENGLISH)
                .build();

		setId(user, memberId);
		setId(other, otherId);

		ExpressionBook othersBook = ExpressionBook.builder()
				.name("Other's Book")
				.language(Language.ENGLISH)
				.member(other)
				.build();
		setId(othersBook, bookId);

		// 반드시 2번 호출되는 repository mocking 보장
		given(memberRepository.findById(memberId)).willReturn(Optional.of(user));
		given(expressionBookRepository.findAllById(List.of(bookId))).willReturn(List.of(othersBook));
		// 기본 표현함 아닌 경우에도 보통 아래처럼 ID 하나로도 조회함
		given(expressionBookRepository.findById(bookId)).willReturn(Optional.of(othersBook));

		// when & then
		ServiceException ex = assertThrows(ServiceException.class, () ->
				expressionBookService.getExpressionsByBook(List.of(bookId), memberId)
		);

		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_EXPRESSION_BOOK);
	}

	@Test
	@DisplayName("getExpressionsByBook(): 단일 표현함 ID로 조회하면 해당 표현들을 최신순으로 반환한다")
	void getExpressionsByBook_withSingleExpressionBook() {
		// given
		Long memberId = 1L;
		Long bookId = 10L;

		Member member = Member
                .builder()
                .language(Language.ENGLISH)
                .build();
		setId(member, memberId);

		ExpressionBook book = ExpressionBook.builder()
				.name("My Book")
				.language(Language.ENGLISH)
				.member(member)
				.build();
		setId(book, bookId);

		ExpressionBookItem item = ExpressionBookItem.builder()
				.expressionBookId(bookId)
				.expressionId(101L)
				.build();
		setId(item, "createdAt", LocalDateTime.of(2025, 5, 16, 10, 0));

		Expression expression = Expression.builder()
				.sentence("단일 표현")
				.description("설명")
				.sentenceAnalysis("분석")
				.subtitleAt(LocalTime.of(0, 0, 5))
				.build();
		setId(expression, 101L);

		// Mock 설정 (존재 검증 + 소유자 검증 + 데이터 조회)
		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		given(expressionBookRepository.findAllById(List.of(bookId))).willReturn(List.of(book)); // ❗ 추가 필요!
		given(expressionBookRepository.findById(bookId)).willReturn(Optional.of(book));
		given(expressionBookItemRepository.findAllById_ExpressionBookIdOrderByCreatedAtDesc(bookId))
				.willReturn(List.of(item));
		given(expressionRepository.findAllById(List.of(101L))).willReturn(List.of(expression));

		// when
		List<ExpressionResponse> result = expressionBookService.getExpressionsByBook(List.of(bookId), memberId);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getExpressionId()).isEqualTo(101L);
		assertThat(result.get(0).getSentence()).isEqualTo("단일 표현");
	}

	@Test
	@DisplayName("getExpressionsByBook(): 표현함 ID가 null이면 기본 표현함을 조회해 표현들을 반환한다")
	void getExpressionsByBook_withDefaultExpressionBook() {
		// given
		Long memberId = 1L;
		Long defaultBookId = 999L;

		Member member = Member
                .builder()
                .language(Language.ENGLISH)
                .build();
		setId(member, memberId);

		ExpressionBook defaultBook = ExpressionBook.builder()
				.name("기본 표현함")
				.language(Language.ENGLISH)
				.member(member)
				.build();
		setId(defaultBook, defaultBookId);

		ExpressionBookItem item = ExpressionBookItem.builder()
				.expressionBookId(defaultBookId)
				.expressionId(200L)
				.build();
		setId(item, "createdAt", LocalDateTime.of(2025, 5, 16, 15, 0));

		Expression expression = Expression.builder()
				.sentence("기본 표현")
				.description("기본 설명")
				.sentenceAnalysis("기본 분석")
				.subtitleAt(LocalTime.of(0, 0, 7))
				.build();
		setId(expression, 200L);

		// mocking
		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		given(expressionBookRepository.findByMemberAndNameAndLanguage(member, "기본 표현함", Language.ENGLISH))
				.willReturn(Optional.of(defaultBook));
		given(expressionBookItemRepository.findAllById_ExpressionBookIdOrderByCreatedAtDesc(defaultBookId))
				.willReturn(List.of(item));
		given(expressionRepository.findAllById(List.of(200L))).willReturn(List.of(expression));

		// when
		List<ExpressionResponse> result = expressionBookService.getExpressionsByBook(null, memberId);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getExpressionId()).isEqualTo(200L);
		assertThat(result.get(0).getSentence()).isEqualTo("기본 표현");
	}

	public static void setId(Object target, Object value) {
		ReflectionTestUtils.setField(target, "id", value);
	}

	public static void setId(Object target, String fieldName, Object value) {
		ReflectionTestUtils.setField(target, fieldName, value);
	}

	@Test
	@DisplayName("delete()는 표현함과 그 안의 표현 아이템들을 모두 삭제한다")
	void deleteExpressionBook_andItsItems() throws Exception {
		// given
		Long memberId = 1L;
		Long bookId = 10L;

		Member member = Member.builder()
			.email("user@test.com")
			.nickname("user")
			.loginPlatform(LoginPlatform.KAKAO)
			.language(Language.ENGLISH)
			.build();
		member.updateSubscription(SubscriptionType.STANDARD);
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
		member.updateSubscription(SubscriptionType.STANDARD);

		Field idField = Member.class.getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(member, 1L);

		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

		ExpressionBookRequest request = new ExpressionBookRequest("기본 표현함");

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
		member.updateSubscription(SubscriptionType.STANDARD);

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
			.nickname("tester")
			.profileImageUrl(null)
			.loginPlatform(LoginPlatform.KAKAO)
			.language(Language.ENGLISH)
			.build();
		mockMember.updateSubscription(SubscriptionType.STANDARD);

		Field idField = Member.class.getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(mockMember, memberId);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
		when(expressionBookRepository.existsByMemberAndName(mockMember, duplicateName)).thenReturn(true);

		ExpressionBookRequest request = new ExpressionBookRequest(duplicateName);

		// when & then
		ServiceException exception = assertThrows(ServiceException.class, () -> {
			expressionBookService.create(request, memberId);
		});

		assertEquals(ErrorCode.DUPLICATE_EXPRESSIONBOOK_NAME, exception.getErrorCode());
		verify(expressionBookRepository, never()).save(any());
	}

	@Test
	@DisplayName("searchExpressions()은 키워드가 포함된 표현을 최신순으로 반환한다")
	void searchExpressions_shouldReturnMatchedExpressionsSortedByCreatedAt() {
		// given
		Long memberId = 1L;
		String keyword = "hello";

		Expression expression1 = Expression.builder()
				.sentence("hello world")
				.description("인사")
				.sentenceAnalysis("분석1")
				.subtitleAt(LocalTime.of(0, 0, 8))
				.videos(null)
				.build();
		setId(expression1, 1L);

		Expression expression2 = Expression.builder()
				.sentence("say hello to my little friend")
				.description("인사2")
				.sentenceAnalysis("분석2")
				.subtitleAt(LocalTime.of(0, 0, 10))
				.videos(null)
				.build();
		setId(expression2, 2L);

		ExpressionBookItem item1 = ExpressionBookItem.builder()
				.expressionBookId(10L)
				.expressionId(1L)
				.build();
		ExpressionBookItem item2 = ExpressionBookItem.builder()
				.expressionBookId(10L)
				.expressionId(2L)
				.build();
		ReflectionTestUtils.setField(item1, "createdAt", LocalDateTime.of(2025, 5, 13, 12, 0));
		ReflectionTestUtils.setField(item2, "createdAt", LocalDateTime.of(2025, 5, 13, 13, 0)); // 최신

		// mocking
		given(expressionBookItemRepository.findByMemberIdAndKeyword(memberId, keyword))
				.willReturn(List.of(item1, item2));
		given(expressionRepository.findAllById(List.of(1L, 2L)))
				.willReturn(List.of(expression1, expression2));

		// when
		List<ExpressionResponse> result = expressionBookService.searchExpressions(memberId, keyword);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getExpressionId()).isEqualTo(2L); // 최신 createdAt 먼저
		assertThat(result.get(1).getExpressionId()).isEqualTo(1L);
	}
}
