package com.mallang.mallang_backend.domain.voca.wordbook.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordToWordbookListRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordDeleteRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordMoveRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordResponse;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordbookCreateRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordbookRenameRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordbookResponse;
import com.mallang.mallang_backend.domain.voca.wordbook.service.WordbookService;
import com.mallang.mallang_backend.domain.voca.wordbookitem.repository.WordbookItemRepository;
import com.mallang.mallang_backend.global.dto.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/wordbooks")
@RequiredArgsConstructor
@Tag(name = "Wordbook", description = "단어장 관리 API")
public class WordbookController {

	private final WordbookService wordbookService;
	private final MemberRepository memberRepository;
	private final WordbookItemRepository wordbookItemRepository;

	/**
	 * 영상 학습 중 1개 이상의 단어를 추가합니다.
	 * @param wordbookId 단어들을 추가할 단어장 ID
	 * @param request 영상 ID, 추가할 단어, 단어의 원래 문장 Request 객체
	 * @return 단어 추가 성공 응답
	 */
	@PostMapping("/{wordbookId}/words")
	@Operation(
		summary = "학습 중 단어 추가",
		description = "영상 학습 중 선택한 하나 이상의 단어를 지정된 단어장에 추가합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "단어 추가 성공")
		}
	)
	public ResponseEntity<RsData<Void>> addWords(
		@Parameter(description = "단어장을 식별할 ID", example = "1")
		@PathVariable Long wordbookId,
		@RequestBody AddWordToWordbookListRequest request
	) {
		// 추후 인증 필터 추가되면 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		wordbookService.addWords(wordbookId, request, member);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장에 단어가 추가되었습니다."
		));
	}

	/**
	 * 회원이 직접 입력한 단어를 추가합니다.
	 * @param wordbookId 단어들을 추가할 단어장 ID
	 * @param request 추가할 단어 객체
	 * @return 단어 추가 성공 응답
	 */
	@PostMapping("/{wordbookId}/words/custom")
	@Operation(
		summary = "사용자 단어 직접 추가",
		description = "회원이 직접 입력한 단어를 지정된 단어장에 추가합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "단어 추가 성공")
		}
	)
	public ResponseEntity<RsData<Void>> addWordCustom(
		@Parameter(description = "단어장을 식별할 ID", example = "1")
		@PathVariable Long wordbookId,
		@RequestBody AddWordRequest request
	) {
		// 추후 인증 필터 추가되면 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		wordbookService.addWordCustom(wordbookId, request, member);

		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장에 단어가 추가되었습니다."
		));
	}

	/**
	 * 추가 단어장 생성
	 * @param request 추가할 단어장 이름
	 * @return 생성 성공 응답, 생성된 단어장 ID
	 */
	@PostMapping
	@Operation(
		summary = "단어장 생성",
		description = "새로운 단어장을 생성하고 생성된 ID를 반환합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "단어장 생성 성공")
		}
	)
	public ResponseEntity<RsData<Long>> createWordbook(
		@RequestBody WordbookCreateRequest request
	) {
		// 추후 인증 필터 추가되면 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		Long id = wordbookService.createWordbook(request, member);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"추가 단어장이 생성되었습니다.",
			id
		));
	}

	/**
	 * 단어장 이름 변경
	 * @param request 변경할 이름
	 * @return 변경 성공 응답
	 */
	@PatchMapping("/{wordbookId}")
	@Operation(
		summary = "단어장 이름 변경",
		description = "지정된 단어장의 이름을 수정합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "이름 변경 성공")
		}
	)
	public ResponseEntity<RsData<Void>> renameWordbook(
		@Parameter(description = "수정할 단어장 ID", example = "1")
		@PathVariable Long wordbookId,
		@RequestBody WordbookRenameRequest request
	) {
		// 추후 인증 필터 추가되면 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		wordbookService.renameWordbook(wordbookId, request.getName(), member);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장의 이름이 변경되었습니다."
		));
	}

	/**
	 * 추가 단어장 삭제
	 * @param wordbookId 삭제할 단어장 ID
	 * @return 삭제 성공 응답
	 */
	@DeleteMapping("/{wordbookId}")
	@Operation(
		summary = "단어장 삭제",
		description = "지정된 단어장을 삭제합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "삭제 성공")
		}
	)
	public ResponseEntity<RsData<Void>> deleteWordbook(
		@Parameter(description = "삭제할 단어장 ID", example = "1")
		@PathVariable Long wordbookId
	) {
		// 추후 인증 필터 추가되면 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		wordbookService.deleteWordbook(wordbookId, member);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장이 삭제되었습니다."
		));
	}

	/**
	 * 단어장의 단어를 다른 단어장으로 이동합니다.
	 * @param request 목적지 단어장 ID, 기존 단어장 ID, 단어
	 * @return 단어 이동 성공 응답
	 */
	@PatchMapping("/words/move")
	@Operation(
		summary = "단어 이동",
		description = "한 단어장에서 다른 단어장으로 단어들을 이동합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "이동 성공")
		}
	)
	public ResponseEntity<RsData<Void>> moveWords(
		@RequestBody WordMoveRequest request
	) {
		// 추후 인증 필터 추가되면 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		wordbookService.moveWords(request, member);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어들이 이동되었습니다."
		));
	}

	@PostMapping("/words/delete")
	@Operation(
		summary = "단어 일괄 삭제",
		description = "단어장 내 여러 단어를 한 번에 삭제합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "삭제 성공")
		}
	)
	public ResponseEntity<RsData<Void>> deleteWords(
		@RequestBody WordDeleteRequest request
	) {
		// 추후 인증 필터 추가되면 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		wordbookService.deleteWords(request, member);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어들이 삭제되었습니다."
		));
	}

	/**
	 * 단어장의 단어들을 조회한다. 단어의 순서는 무작위로 섞인다.
	 * @param wordbookId 단어장 ID
	 * @return 단어장의 단어들 리스트
	 */
	@GetMapping("/{wordbookId}/words")
	@Operation(
		summary = "단어 목록 조회",
		description = "지정된 단어장의 단어들을 무작위 순서로 반환합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "조회 성공")
		}
	)
	public ResponseEntity<RsData<List<WordResponse>>> getWords(
		@Parameter(description = "조회할 단어장 ID", example = "1")
		@PathVariable Long wordbookId
	) {
		// 추후 인증 필터 적용 후 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		List<WordResponse> words = wordbookService.getWordsRandomly(wordbookId, member);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어 목록이 조회되었습니다.",
			words
		));
	}

	/**
	 * 사용자의 단어장 목록 조회
	 * @return 단어장 리스트
	 */
	@GetMapping
	@Operation(
		summary = "단어장 목록 조회",
		description = "로그인한 사용자의 모든 단어장을 조회합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "조회 성공")
		}
	)
	public ResponseEntity<RsData<List<WordbookResponse>>> getWordbooks() {
		// 추후 인증 필터 적용 시 로그인 사용자로 교체
		Member member = memberRepository.findById(1L).get();

		List<WordbookResponse> wordbooks = wordbookService.getWordbooks(member);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장 목록 조회에 성공했습니다.",
			wordbooks
		));
	}
}
