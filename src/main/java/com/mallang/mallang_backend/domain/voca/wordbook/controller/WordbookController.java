package com.mallang.mallang_backend.domain.voca.wordbook.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordToWordbookListRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.service.WordbookService;
import com.mallang.mallang_backend.global.dto.RsData;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/wordbooks")
@RequiredArgsConstructor
public class WordbookController {

	private final WordbookService wordbookService;

	/**
	 * 영상 학습 중 1개 이상의 단어를 추가합니다.
	 * @param wordbookId 단어들을 추가할 단어장 ID
	 * @param request 영상 ID, 추가할 단어, 단어의 원래 문장 Request 객체
	 * @param member 로그인한 회원
	 * @return 단어 추가 성공 응답
	 */
	@PostMapping("/{wordbookId}/words")
	public ResponseEntity<RsData<Void>> addWords(
		@PathVariable Long wordbookId,
		@RequestBody AddWordToWordbookListRequest request,
		@AuthenticationPrincipal Member member
	) {
		wordbookService.addWords(wordbookId, request, member);
		return ResponseEntity.ok(new RsData<>(
			"200-1",
			"단어장에 단어가 추가되었습니다."
		));
	}
}
