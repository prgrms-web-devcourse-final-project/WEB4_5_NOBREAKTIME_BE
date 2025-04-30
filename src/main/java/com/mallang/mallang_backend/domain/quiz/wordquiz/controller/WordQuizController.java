package com.mallang.mallang_backend.domain.quiz.wordquiz.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizResponse;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizResultSaveRequest;
import com.mallang.mallang_backend.domain.quiz.wordquiz.service.WordQuizService;
import com.mallang.mallang_backend.global.dto.RsData;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class WordQuizController {

	private final WordQuizService wordQuizService;
	private final MemberRepository memberRepository;

	/**
	 * 단어장에 대한 퀴즈를 요청합니다.
	 * @param wordbookId 단어장 ID
	 * @return 단어장에 대한 퀴즈
	 */
	@GetMapping("/wordbooks/{wordbookId}")
	public ResponseEntity<RsData<WordQuizResponse>> getWordbookQuiz(
		@PathVariable Long wordbookId
	) {
		// TODO: 실제 인증 적용 후 대체
		Member member = memberRepository.findById(1L).get();

		WordQuizResponse quizResponse = wordQuizService.generateWordbookQuiz(wordbookId, member);

		return ResponseEntity.ok(new RsData<>(
			"200-1",
			"단어장 퀴즈 문제를 조회했습니다.",
			quizResponse
		));
	}

	@PostMapping("/wordbook/result")
	public ResponseEntity<RsData<Void>> saveWordbookQuizResult(
		@RequestBody WordQuizResultSaveRequest request
	) {
		// TODO: 인증 후 member 교체
		Member member = memberRepository.findById(1L).orElseThrow();

		wordQuizService.saveWordbookQuizResult(request, member);
		return ResponseEntity.ok(new RsData<>(
			"200-1",
			"단어장 퀴즈 결과 저장 완료"
		));
	}

}
