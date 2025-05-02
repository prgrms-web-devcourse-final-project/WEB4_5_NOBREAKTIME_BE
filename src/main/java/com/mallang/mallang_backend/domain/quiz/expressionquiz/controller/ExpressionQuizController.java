package com.mallang.mallang_backend.domain.quiz.expressionquiz.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.service.MemberService;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.service.ExpressionQuizService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.Login;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/expressionbooks")
@RequiredArgsConstructor
public class ExpressionQuizController {

	private final MemberService memberService;
	private final ExpressionQuizService expressionQuizService;

	/**
	 * 표현함에 대한 퀴즈를 요청합니다.
	 * @param expressionBookId 표현함 ID
	 * @param userDetail 로그인한 사용자의 정보
	 * @return 표현함 퀴즈 문제
	 */
	@GetMapping("/{expressionBookId}/quiz")
	public ResponseEntity<RsData<ExpressionQuizResponse>> getWordbookQuiz(
		@PathVariable Long expressionBookId,
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();
		Member member = memberService.getMemberById(memberId);

		ExpressionQuizResponse quizResponse = expressionQuizService.generateExpressionBookQuiz(expressionBookId, member);

		return ResponseEntity.ok(new RsData<>(
			"200",
			"표현함 퀴즈 문제를 조회했습니다.",
			quizResponse
		));
	}

	/**
	 * 표현함 아이템 대한 퀴즈 결과를 저장합니다.
	 * @param request 표현함 아이템별 퀴즈 결과
	 * @param userDetail 로그인한 사용자의 정보
	 * @return 퀴즈 결과 저장 완료
	 */
	@PostMapping("/quiz/result")
	public ResponseEntity<RsData<Void>> saveExpressionQuizResult(
		@RequestBody ExpressionQuizResultSaveRequest request,
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();
		Member member = memberService.getMemberById(memberId);

		expressionQuizService.saveExpressionQuizResult(request, member);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"표현함 퀴즈 결과 저장 완료"
		));
	}
}
