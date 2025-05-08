package com.mallang.mallang_backend.domain.quiz.wordquiz.controller;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.service.MemberService;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizResponse;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizResultSaveRequest;
import com.mallang.mallang_backend.domain.quiz.wordquiz.service.WordQuizService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.Login;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "WordQuiz", description = "단어 퀴즈 관련 API")
@RestController
@RequestMapping("/api/v1/wordbooks")
@RequiredArgsConstructor
public class WordQuizController {

	private final WordQuizService wordQuizService;
	private final MemberService memberService;

	/**
	 * 단어장에 대한 퀴즈를 요청합니다.
	 * @param wordbookId 단어장 ID
	 * @param userDetail 로그인한 사용자의 정보
	 * @return 단어장 퀴즈 문제
	 */
	@Operation(summary = "단어장 퀴즈 조회", description = "단어장에 대한 퀴즈를 요청합니다.")
	@ApiResponse(responseCode = "200", description = "단어장 퀴즈 문제를 조회했습니다.")
	@PossibleErrors({NO_WORDBOOK_EXIST_OR_FORBIDDEN, WORDBOOK_IS_EMPTY})
	@GetMapping("/{wordbookId}/quiz")
	public ResponseEntity<RsData<WordQuizResponse>> getWordbookQuiz(
		@PathVariable Long wordbookId,
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();
		Member member = memberService.getMemberById(memberId);

		WordQuizResponse quizResponse = wordQuizService.generateWordbookQuiz(wordbookId, member);

		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장 퀴즈 문제를 조회했습니다.",
			quizResponse
		));
	}

	/**
	 * 단어장 아이템에 대한 퀴즈 결과를 저장합니다.
	 * @param request 단어장아이템별 퀴즈 결과
	 * @param userDetail 로그인한 사용자의 정보
	 * @return 퀴즈 결과 저장 완료
	 */
	@Operation(summary = "단어장 퀴즈 결과 저장", description = "단어장 아이템에 대한 퀴즈 결과를 저장합니다.")
	@ApiResponse(responseCode = "200", description = "단어장 퀴즈 결과 저장 완료")
	@PossibleErrors({WORDBOOK_ITEM_NOT_FOUND, WORDQUIZ_NOT_FOUND})
	@PostMapping("/quiz/result")
	public ResponseEntity<RsData<Void>> saveWordbookQuizResult(
		@RequestBody WordQuizResultSaveRequest request,
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();
		Member member = memberService.getMemberById(memberId);

		wordQuizService.saveWordbookQuizResult(request, member);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"단어장 퀴즈 결과 저장 완료"
		));
	}

	/**
	 * 통합(오늘의 학습) 퀴즈를 요청합니다.
	 * @param userDetail 로그인한 사용자의 정보
	 * @return 통합 퀴즈 문제
	 */
	@Operation(summary = "통합 퀴즈 조회", description = "통합(오늘의 학습) 퀴즈를 요청합니다.")
	@ApiResponse(responseCode = "200", description = "통합 퀴즈 문제를 조회했습니다.")
	@PossibleErrors({NOT_ENOUGH_WORDS_FOR_QUIZ})
	@GetMapping("/quiz/total")
	public ResponseEntity<RsData<WordQuizResponse>> getWordbookTotalQuiz(
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();
		Member member = memberService.getMemberById(memberId);

		WordQuizResponse quizResponse = wordQuizService.generateWordbookTotalQuiz(member);

		return ResponseEntity.ok(new RsData<>(
			"200",
			"통합 퀴즈 문제를 조회했습니다.",
			quizResponse
		));
	}

	/**
	 * 통합(오늘의 학습) 퀴즈 결과를 저장합니다.
	 * @param request 단어장아이템별 퀴즈 결과
	 * @param userDetail 로그인한 사용자의 정보
	 * @return 통합 퀴즈 결과 저장 완료
	 */
	@Operation(summary = "통합 퀴즈 결과 저장", description = "통합(오늘의 학습) 퀴즈 결과를 저장합니다.")
	@ApiResponse(responseCode = "200", description = "통합 퀴즈 결과 저장 완료")
	@PossibleErrors({WORDBOOK_ITEM_NOT_FOUND, WORDQUIZ_NOT_FOUND})
	@PostMapping("/quiz/total/result")
	public ResponseEntity<RsData<Void>> saveWordbookTotalQuizResult(
		@RequestBody WordQuizResultSaveRequest request,
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();
		Member member = memberService.getMemberById(memberId);

		wordQuizService.saveWordbookTotalQuizResult(request, member);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"통합 퀴즈 결과 저장 완료"
		));
	}
}
