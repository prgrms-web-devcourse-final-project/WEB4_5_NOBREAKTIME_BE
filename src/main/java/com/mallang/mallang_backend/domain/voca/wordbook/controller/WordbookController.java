package com.mallang.mallang_backend.domain.voca.wordbook.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordToWordbookListRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordbookCreateRequest;
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
	 * @param request
	 * @return
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
			"단어장에 단어가 추가되었습니다.",
			id
		));
	}
}
