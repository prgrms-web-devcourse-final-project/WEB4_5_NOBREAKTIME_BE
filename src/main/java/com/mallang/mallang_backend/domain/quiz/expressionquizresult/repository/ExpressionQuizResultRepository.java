package com.mallang.mallang_backend.domain.quiz.expressionquizresult.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.expressionquizresult.entity.ExpressionQuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExpressionQuizResultRepository extends JpaRepository<ExpressionQuizResult, Long> {

	int countByExpressionQuiz_Member(Member expressionQuizMember);

	int countByExpressionQuiz_MemberAndCreatedAtAfter(Member expressionQuizMember, LocalDateTime localDateTime);

	List<ExpressionQuizResult> findByExpressionQuiz_MemberAndCreatedAtAfter(Member member, LocalDateTime localDateTime);
}
