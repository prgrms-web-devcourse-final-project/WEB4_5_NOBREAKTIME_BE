package com.mallang.mallang_backend.domain.quiz.expressionquizresult.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.expressionquizresult.entity.ExpressionQuizResult;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;

public interface ExpressionQuizResultRepository extends JpaRepository<ExpressionQuizResult, Long> {

	int countByExpressionQuiz_Member(Member expressionQuizMember);

	int countByExpressionQuiz_MemberAndCreatedAtAfter(Member expressionQuizMember, LocalDateTime localDateTime);

	List<ExpressionQuizResult> findByExpressionQuiz_MemberAndCreatedAtAfter(Member member, LocalDateTime localDateTime);

	void deleteAllByExpression_IdInAndExpressionBook(List<Long> expressionIds, ExpressionBook expressionBook);

	List<ExpressionQuizResult> findAllByExpression_IdInAndExpressionBook(List<Long> expressionIds, ExpressionBook expressionBook);

	void deleteAllByExpressionBook(ExpressionBook expressionBook);

	List<ExpressionQuizResult> findTop100ByExpressionQuiz_MemberAndCreatedAtAfterOrderByCreatedAtDesc(Member member, LocalDateTime measuredAt);
}
