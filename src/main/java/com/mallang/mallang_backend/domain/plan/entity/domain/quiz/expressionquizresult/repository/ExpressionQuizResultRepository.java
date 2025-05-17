package com.mallang.mallang_backend.domain.plan.entity.domain.quiz.expressionquizresult.repository;

import com.mallang.mallang_backend.domain.plan.entity.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.plan.entity.domain.quiz.expressionquizresult.entity.ExpressionQuizResult;
import com.mallang.mallang_backend.domain.plan.entity.domain.sentence.expressionbook.entity.ExpressionBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExpressionQuizResultRepository extends JpaRepository<ExpressionQuizResult, Long> {

	int countByExpressionQuiz_MemberAndCreatedAtAfter(Member expressionQuizMember, LocalDateTime localDateTime);

	List<ExpressionQuizResult> findByExpressionQuiz_MemberAndCreatedAtAfter(Member member, LocalDateTime localDateTime);

	void deleteAllByExpression_IdInAndExpressionBook(List<Long> expressionIds, ExpressionBook expressionBook);

	List<ExpressionQuizResult> findAllByExpression_IdInAndExpressionBook(List<Long> expressionIds, ExpressionBook expressionBook);

	void deleteAllByExpressionBook(ExpressionBook expressionBook);

	List<ExpressionQuizResult> findTop100ByExpressionQuiz_MemberAndCreatedAtAfterOrderByCreatedAtDesc(Member member, LocalDateTime measuredAt);
}
