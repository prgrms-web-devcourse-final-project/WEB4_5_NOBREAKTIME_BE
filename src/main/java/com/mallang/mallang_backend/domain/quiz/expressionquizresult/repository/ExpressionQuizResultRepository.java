package com.mallang.mallang_backend.domain.quiz.expressionquizresult.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.entity.ExpressionQuiz;
import com.mallang.mallang_backend.domain.quiz.expressionquizresult.entity.ExpressionQuizResult;
import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExpressionQuizResultRepository extends JpaRepository<ExpressionQuizResult, Long> {

	int countByExpressionQuiz_MemberAndCreatedAtAfter(Member expressionQuizMember, LocalDateTime localDateTime);

	List<ExpressionQuizResult> findByExpressionQuiz_MemberAndCreatedAtAfter(Member member, LocalDateTime localDateTime);

	void deleteAllByExpression_IdInAndExpressionBook(List<Long> expressionIds, ExpressionBook expressionBook);

	List<ExpressionQuizResult> findAllByExpression_IdInAndExpressionBook(List<Long> expressionIds, ExpressionBook expressionBook);

	void deleteAllByExpressionBook(ExpressionBook expressionBook);

	List<ExpressionQuizResult> findTop100ByExpressionQuiz_MemberAndCreatedAtAfterOrderByCreatedAtDesc(Member member, LocalDateTime measuredAt);

	Optional<ExpressionQuizResult> findByExpressionAndExpressionBookAndExpressionQuiz(Expression expression, ExpressionBook expressionBook, ExpressionQuiz expressionQuiz);
}
