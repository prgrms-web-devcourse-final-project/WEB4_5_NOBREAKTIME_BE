package com.mallang.mallang_backend.domain.quiz.expressionquiz.service;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.dto.ExpressionQuizResponse;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.dto.ExpressionQuizResultSaveRequest;

public interface ExpressionQuizService {
	ExpressionQuizResponse generateExpressionBookQuiz(Long expressionBookId, Member member);

	void saveExpressionQuizResult(ExpressionQuizResultSaveRequest request, Member member);
}
