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
import com.mallang.mallang_backend.global.dto.RsData;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/wordbooks")
@RequiredArgsConstructor
public class WordbookController {

	private final WordbookService wordbookService;
	private final MemberRepository memberRepository;

	/**
	 * 영상 학습 중 1개 이상의 단어를 추가합니다.
	 * @param wordbookId 단어들을 추가할 단어장 ID
	 * @param request 영상 ID, 추가할 단어, 단어의 원래 문장 Request 객체
	 * @return 단어 추가 성공 응답
	 */
	@PostMapping("/{wordbookId}/words")
	public ResponseEntity<RsData<Void>> addWords(
		@PathVariable Long wordbookId,
		@RequestBody AddWordToWordbookListRequest request
	) {
		// 추후 인증 필터 추가되면 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		wordbookService.addWords(wordbookId, request, member);
		return ResponseEntity.ok(new RsData<>(
			"200-1",
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
	public ResponseEntity<RsData<Void>> addWordCustom(
		@PathVariable Long wordbookId,
		@RequestBody AddWordRequest request
	) {
		// 추후 인증 필터 추가되면 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		wordbookService.addWordCustom(wordbookId, request, member);

		return ResponseEntity.ok(new RsData<>(
			"200-1",
			"단어장에 단어가 추가되었습니다."
		));
	}

	/**
	 * 추가 단어장 생성
	 * @param request 추가할 단어장 이름
	 * @return 생성 성공 응답, 생성된 단어장 ID
	 */
	@PostMapping
	public ResponseEntity<RsData<Long>> createWordbook(
		@RequestBody WordbookCreateRequest request
	) {
		// 추후 인증 필터 추가되면 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		Long id = wordbookService.createWordbook(request, member);
		return ResponseEntity.ok(new RsData<>(
			"200-1",
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
	public ResponseEntity<RsData<Void>> renameWordbook(
		@PathVariable Long wordbookId,
		@RequestBody WordbookRenameRequest request
	) {
		// 추후 인증 필터 추가되면 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		wordbookService.renameWordbook(wordbookId, request.getName(), member);
		return ResponseEntity.ok(new RsData<>(
			"200-1",
			"단어장의 이름이 변경되었습니다."
		));
	}

	/**
	 * 추가 단어장 삭제
	 * @param wordbookId 삭제할 단어장 ID
	 * @return 삭제 성공 응답
	 */
	@DeleteMapping("/{wordbookId}")
	public ResponseEntity<RsData<Void>> deleteWordbook(@PathVariable Long wordbookId) {
		// 추후 인증 필터 추가되면 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		wordbookService.deleteWordbook(wordbookId, member);
		return ResponseEntity.ok(new RsData<>(
			"200-1",
			"단어장이 삭제되었습니다."
		));
	}

	/**
	 * 단어장의 단어를 다른 단어장으로 이동합니다.
	 * @param request 목적지 단어장 ID, 기존 단어장 ID, 단어
	 * @return 단어 이동 성공 응답
	 */
	@PatchMapping("/words/move")
	public ResponseEntity<RsData<Void>> moveWords(
		@RequestBody WordMoveRequest request
	) {
		// 추후 인증 필터 추가되면 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		wordbookService.moveWords(request, member);
		return ResponseEntity.ok(new RsData<>(
			"200-1",
			"단어들이 이동되었습니다."
		));
	}

	/**
	 * 단어장 내 단어 일괄 삭제
	 * @param request 삭제할 단어들의 단어장 ID, 단어
	 * @return 삭제 성공 응답
	 */
	@PostMapping("/words/delete")
	public ResponseEntity<RsData<Void>> deleteWords(
		@RequestBody WordDeleteRequest request
	) {
		// 추후 인증 필터 추가되면 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		wordbookService.deleteWords(request, member);
		return ResponseEntity.ok(new RsData<>(
			"200-1",
			"단어들이 삭제되었습니다."
		));
	}

	/**
	 * 단어장의 단어들을 조회한다. 단어의 순서는 무작위로 섞인다.
	 * @param wordbookId 단어장 ID
	 * @return 단어장의 단어들 리스트
	 */
	@GetMapping("/{wordbookId}/words")
	public ResponseEntity<RsData<List<WordResponse>>> getWords(
		@PathVariable Long wordbookId
	) {
		// 추후 인증 필터 적용 후 로그인한 회원으로 변경
		Member member = memberRepository.findById(1L).get();

		List<WordResponse> words = wordbookService.getWordsRandomly(wordbookId, member);
		return ResponseEntity.ok(new RsData<>(
			"200-1",
			"단어 목록이 조회되었습니다.",
			words
		));
	}

	/**
	 * 사용자의 단어장 목록 조회
	 * @return 단어장 리스트
	 */
	@GetMapping
	public ResponseEntity<RsData<List<WordbookResponse>>> getWordbooks() {
		// 추후 인증 필터 적용 시 로그인 사용자로 교체
		Member member = memberRepository.findById(1L).get();

		List<WordbookResponse> wordbooks = wordbookService.getWordbooks(member);
		return ResponseEntity.ok(new RsData<>(
			"200-1",
			"단어장 목록 조회에 성공했습니다.",
			wordbooks
		));
	}
}
