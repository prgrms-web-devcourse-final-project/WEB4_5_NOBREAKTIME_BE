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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Tag(name = "WordQuiz", description = "단어 퀴즈 관련 API")
public class WordQuizController {

	private final WordQuizService wordQuizService;
	private final MemberRepository memberRepository;

	/**
	 * 단어장에 대한 퀴즈를 요청합니다.
	 * @param wordbookId 단어장 ID
	 * @return 단어장에 대한 퀴즈
	 */
	@GetMapping("/wordbooks/{wordbookId}")
	@Operation(
		summary = "단어장 퀴즈 조회",
		description = "단어장 ID에 해당하는 퀴즈 문제를 조회합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "조회 성공"),
		}
	)
	public ResponseEntity<RsData<WordQuizResponse>> getWordbookQuiz(
		@Parameter(description = "조회할 단어장 ID", example = "1")
		@PathVariable Long wordbookId
	) {
		// TODO: 실제 인증 적용 후 대체
		Member member = memberRepository.findFirstByOrderByIdAsc();

		WordQuizResponse quizResponse = wordQuizService.generateWordbookQuiz(wordbookId, member);

		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장 퀴즈 문제를 조회했습니다.",
			quizResponse
		));
	}

	/**
	 * 단어장 아이템 대한 퀴즈 결과를 저장합니다.
	 * @param request 단어장아이템별 퀴즈 결과
	 * @return 퀴즈 결과 저장 완료
	 */
	@PostMapping("/wordbook/result")
	@Operation(
		summary = "단어장 퀴즈 결과 저장",
		description = "단어장 퀴즈 풀이 결과를 저장합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "저장 완료")
		}
	)
	public ResponseEntity<RsData<Void>> saveWordbookQuizResult(
		@RequestBody WordQuizResultSaveRequest request
	) {
		// TODO: 인증 후 member 교체
		Member member = memberRepository.findFirstByOrderByIdAsc();

		wordQuizService.saveWordbookQuizResult(request, member);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장 퀴즈 결과 저장 완료"
		));
	}

	/**
	 * 통합 단어장 퀴즈를 요청합니다.
	 * @return 통합 단어장 퀴즈
	 */
	@GetMapping("/total")
	@Operation(
		summary = "통합 퀴즈 조회",
		description = "모든 단어장에 대한 통합 퀴즈를 조회합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "조회 성공")
		}
	)
	public ResponseEntity<RsData<WordQuizResponse>> getWordbookTotalQuiz(
	) {
		// TODO: 실제 인증 적용 후 대체
		Member member = memberRepository.findFirstByOrderByIdAsc();

		WordQuizResponse quizResponse = wordQuizService.generateWordbookTotalQuiz(member);

		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장 통합 퀴즈 문제를 조회했습니다.",
			quizResponse
		));
	}

	/**
	 * 단어장 아이템 대한 통합 퀴즈 결과를 저장합니다.
	 * @param request 단어장아이템별 퀴즈 결과
	 * @return 퀴즈 결과 저장 완료
	 */
	@PostMapping("/wordbook/total/result")
	@Operation(
		summary = "통합 퀴즈 결과 저장",
		description = "통합 단어장 퀴즈 풀이 결과를 저장합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "저장 완료")
		}
	)
	public ResponseEntity<RsData<Void>> saveWordbookTotalQuizResult(
		@RequestBody WordQuizResultSaveRequest request
	) {
		// TODO: 인증 후 member 교체
		Member member = memberRepository.findFirstByOrderByIdAsc();

		wordQuizService.saveWordbookTotalQuizResult(request, member);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장 통합 퀴즈 결과 저장 완료"
		));
	}
}
