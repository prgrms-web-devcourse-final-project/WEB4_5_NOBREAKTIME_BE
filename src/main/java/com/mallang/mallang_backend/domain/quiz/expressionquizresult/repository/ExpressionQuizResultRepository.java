package com.mallang.mallang_backend.domain.quiz.expressionquizresult.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.expressionquizresult.entity.ExpressionQuizResult;

public interface ExpressionQuizResultRepository extends JpaRepository<ExpressionQuizResult, Long> {

	int countByExpressionQuiz_Member(Member expressionQuizMember);

	int countByExpressionQuiz_MemberAndCreatedAtAfter(Member expressionQuizMember, LocalDateTime localDateTime);

	List<ExpressionQuizResult> findByExpressionQuiz_MemberAndCreatedAtAfter(Member member, LocalDateTime localDateTime);

	List<ExpressionQuizResult> findTop100ByExpressionQuiz_MemberAndCreatedAtAfterOrderByCreatedAtDesc(Member member, LocalDateTime measuredAt);
}
