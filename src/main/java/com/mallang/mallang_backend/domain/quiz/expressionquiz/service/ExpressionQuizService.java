package com.mallang.mallang_backend.domain.quiz.expressionquiz.service;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.controller.ExpressionQuizResponse;

public interface ExpressionQuizService {
	ExpressionQuizResponse generateExpressionBookQuiz(Long expressionBookId, Member member);
}
