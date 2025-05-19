package com.mallang.mallang_backend.domain.voca.wordbook.controller;

import com.mallang.mallang_backend.domain.voca.wordbook.dto.*;
import com.mallang.mallang_backend.domain.voca.wordbook.service.WordbookService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.login.Login;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Tag(name = "Wordbook", description = "단어장 관련 API")
@RestController
@RequestMapping("/api/v1/wordbooks")
@RequiredArgsConstructor
public class WordbookController {

	private final WordbookService wordbookService;

	/**
	 * 영상 학습 중 1개 이상의 단어를 추가합니다.
	 * @param wordbookId 단어들을 추가할 단어장 ID
	 * @param request 영상 ID, 추가할 단어, 단어의 원래 문장 Request 객체
	 * @param userDetail 로그인한 회원
	 * @return 단어 추가 성공 응답
	 */
	@Operation(summary = "영상 학습 중 단어 추가", description = "영상 학습 중 1개 이상의 단어를 추가합니다.")
	@ApiResponse(responseCode = "200", description = "단어장에 단어가 추가되었습니다.")
	@PossibleErrors({NO_WORDBOOK_EXIST_OR_FORBIDDEN})
	@PostMapping("/{wordbookId}/words")
	public ResponseEntity<RsData<Void>> addWords(
		@PathVariable Long wordbookId,
		@RequestBody @Valid AddWordToWordbookListRequest request,
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();

		wordbookService.addWords(wordbookId, request, memberId);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장에 단어가 추가되었습니다."
		));
	}

	/**
	 * 회원이 직접 입력한 단어를 추가합니다.
	 * @param wordbookId 단어들을 추가할 단어장 ID
	 * @param request 추가할 단어 객체
	 * @param userDetail 로그인한 회원
	 * @return 단어 추가 성공 응답
	 */
	@Operation(summary = "사용자 정의 단어 추가", description = "회원이 직접 입력한 단어를 추가합니다.")
	@ApiResponse(responseCode = "200", description = "단어장에 단어가 추가되었습니다.")
	@PossibleErrors({NO_WORDBOOK_EXIST_OR_FORBIDDEN})
	@PostMapping("/{wordbookId}/words/custom")
	public ResponseEntity<RsData<Void>> addWordCustom(
		@PathVariable Long wordbookId,
		@RequestBody @Valid AddWordRequest request,
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();

		wordbookService.addWordCustom(wordbookId, request, memberId);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장에 단어가 추가되었습니다."
		));
	}

	/**
	 * 추가 단어장 생성
	 * @param request 추가할 단어장 이름
	 * @param userDetail 로그인한 회원
	 * @return 생성 성공 응답, 생성된 단어장 ID
	 */
	@Operation(summary = "단어장 생성", description = "추가 단어장을 생성합니다.")
	@ApiResponse(responseCode = "200", description = "추가 단어장이 생성되었습니다.")
	@PreAuthorize("hasAnyRole('STANDARD', 'PREMIUM')")
	@PossibleErrors({MEMBER_NOT_FOUND, LANGUAGE_IS_NONE, WORDBOOK_CREATE_DEFAULT_FORBIDDEN, WORDBOOK_CREATE_DEFAULT_FORBIDDEN})
	@PostMapping
	public ResponseEntity<RsData<Long>> createWordbook(
		@RequestBody @Valid WordbookCreateRequest request,
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();

		Long id = wordbookService.createWordbook(request, memberId);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"추가 단어장이 생성되었습니다.",
			id
		));
	}

	/**
	 * 단어장 이름 변경
	 * @param wordbookId 수정할 단어장 ID
	 * @param request 변경할 이름
	 * @param userDetail 로그인한 회원
	 * @return 변경 성공 응답
	 */
	@Operation(summary = "단어장 이름 변경", description = "단어장의 이름을 변경합니다.")
	@ApiResponse(responseCode = "200", description = "단어장의 이름이 변경되었습니다.")
	@PreAuthorize("hasAnyRole('STANDARD', 'PREMIUM')")
	@PossibleErrors({NO_WORDBOOK_EXIST_OR_FORBIDDEN})
	@PatchMapping("/{wordbookId}")
	public ResponseEntity<RsData<Void>> renameWordbook(
		@PathVariable Long wordbookId,
		@RequestBody @Valid WordbookRenameRequest request,
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();

		wordbookService.renameWordbook(wordbookId, request.getName(), memberId);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장의 이름이 변경되었습니다."
		));
	}

	/**
	 * 추가 단어장 삭제
	 * @param wordbookId 삭제할 단어장 ID
	 * @param userDetail 로그인한 회원
	 * @return 삭제 성공 응답
	 */
	@Operation(summary = "단어장 삭제", description = "특정 단어장을 삭제합니다.")
	@ApiResponse(responseCode = "200", description = "단어장이 삭제되었습니다.")
	@PreAuthorize("hasAnyRole('STANDARD', 'PREMIUM')")
	@PossibleErrors({NO_WORDBOOK_EXIST_OR_FORBIDDEN, WORDBOOK_DELETE_DEFAULT_FORBIDDEN})
	@DeleteMapping("/{wordbookId}")
	public ResponseEntity<RsData<Void>> deleteWordbook(
		@PathVariable Long wordbookId,
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();

		wordbookService.deleteWordbook(wordbookId, memberId);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장이 삭제되었습니다."
		));
	}

	/**
	 * 단어장의 단어를 다른 단어장으로 이동합니다.
	 * @param request 목적지 단어장 ID, 기존 단어장 ID 및 단어 리스트
	 * @param userDetail 로그인한 회원
	 * @return 이동 성공 응답
	 */
	@Operation(summary = "단어 이동", description = "단어를 다른 단어장으로 이동합니다.")
	@ApiResponse(responseCode = "200", description = "단어들이 이동되었습니다.")
	@PreAuthorize("hasAnyRole('STANDARD', 'PREMIUM')")
	@PossibleErrors({NO_WORDBOOK_EXIST_OR_FORBIDDEN, WORDBOOK_ITEM_NOT_FOUND})
	@PatchMapping("/words/move")
	public ResponseEntity<RsData<Void>> moveWords(
		@RequestBody @Valid WordMoveRequest request,
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();

		wordbookService.moveWords(request, memberId);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어들이 이동되었습니다."
		));
	}

	/**
	 * 단어장 내 단어 일괄 삭제
	 * @param request 삭제할 단어 리스트 및 단어장 ID
	 * @param userDetail 로그인한 회원
	 * @return 삭제 성공 응답
	 */
	@Operation(summary = "단어 일괄 삭제", description = "단어장을 선택하여 단어들을 일괄 삭제합니다.")
	@ApiResponse(responseCode = "200", description = "단어들이 삭제되었습니다.")
	@PossibleErrors({NO_WORDBOOK_EXIST_OR_FORBIDDEN, WORDBOOK_ITEM_NOT_FOUND})
	@PostMapping("/words/delete")
	public ResponseEntity<RsData<Void>> deleteWords(
		@RequestBody @Valid WordDeleteRequest request,
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();

		wordbookService.deleteWords(request, memberId);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어들이 삭제되었습니다."
		));
	}

	/**
	 * 단어장의 단어들을 조회합니다. 단어 순서는 무작위입니다.
	 * @param wordbookId 단어장 ID
	 * @param userDetail 로그인한 회원
	 * @return 단어 리스트
	 */
	@Operation(summary = "단어 목록 조회", description = "특정 단어장의 단어 목록을 무작위로 조회합니다.")
	@ApiResponse(responseCode = "200", description = "단어 목록이 조회되었습니다.")
	@PossibleErrors({NO_WORDBOOK_EXIST_OR_FORBIDDEN})
	@GetMapping("/{wordbookId}/words")
	public ResponseEntity<RsData<List<WordResponse>>> getWords(
		@PathVariable Long wordbookId,
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();

		List<WordResponse> words = wordbookService.getWordsRandomly(wordbookId, memberId);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어 목록이 조회되었습니다.",
			words
		));
	}

	/**
	 * 사용자의 단어장 목록 조회
	 * @param userDetail 로그인한 회원
	 * @return 단어장 리스트
	 */
	@Operation(summary = "단어장 목록 조회", description = "로그인한 사용자의 모든 단어장을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "단어장 목록 조회에 성공했습니다.")
	@PossibleErrors({MEMBER_NOT_FOUND})
	@GetMapping
	public ResponseEntity<RsData<List<WordbookResponse>>> getWordbooks(
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();

		List<WordbookResponse> wordbooks = wordbookService.getWordbooks(memberId);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장 목록 조회에 성공했습니다.",
			wordbooks
		));
	}

	/**
	 * 단어장 페이지에 접속했을 때, 선택한 단어장의 단어들을 조회합니다. 선택한 단어장이 없으면 "기본" 단어장의 단어를 조회합니다.
	 * @param wordbookIds 단어장 ID 리스트
	 * @param userDetail 로그인한 회원
	 * @return 단어 리스트
	 */
	@Operation(summary = "여러 단어장의 단어 목록 조회", description = "체크된 여러 단어장의 단어들을 등록 날짜 기준으로 정렬하여 조회합니다.")
	@ApiResponse(responseCode = "200", description = "단어 목록이 조회되었습니다.")
	@PossibleErrors({MEMBER_NOT_FOUND, NO_WORDBOOK_EXIST_OR_FORBIDDEN})
	@GetMapping("/view")
	public ResponseEntity<RsData<List<WordResponse>>> getWordbookItems(
		@RequestParam(required = false) List<Long> wordbookIds,
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();

		List<WordResponse> words = wordbookService.getWordbookItems(wordbookIds, memberId);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어 목록이 조회되었습니다.",
			words
		));
	}
}