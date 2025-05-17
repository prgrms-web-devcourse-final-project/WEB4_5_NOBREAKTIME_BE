package com.mallang.mallang_backend.domain.plan.entity.domain.sentence.expressionbookitem.repository;

import com.mallang.mallang_backend.domain.plan.entity.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.plan.entity.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.plan.entity.domain.sentence.expressionbookitem.entity.ExpressionBookItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExpressionBookItemRepository extends JpaRepository<ExpressionBookItem, ExpressionBookItemId> {
    List<ExpressionBookItem> findAllById_ExpressionBookId(Long expressionBookId);

    boolean existsById(ExpressionBookItemId itemId);

    Optional<ExpressionBookItem> findById(ExpressionBookItemId itemId);

    void deleteAllById_ExpressionBookId(Long expressionBookId);

    @Query("SELECT e FROM Expression e " +
            "JOIN ExpressionBookItem bi ON e.id = bi.id.expressionId " +
            "JOIN ExpressionBook eb ON bi.id.expressionBookId = eb.id " +
            "WHERE eb.member.id = :memberId " +
            "AND e.sentence LIKE %:keyword%")
    List<Expression> findExpressionsByMemberAndKeyword(@Param("memberId") Long memberId, @Param("keyword") String keyword);

    List<ExpressionBookItem> findAllById_ExpressionBookIdIn(List<Long> expressionBookIds);

    @Query("""
                SELECT i FROM ExpressionBookItem i
                JOIN ExpressionBook b ON i.id.expressionBookId = b.id
                JOIN Expression e ON i.id.expressionId = e.id
                WHERE b.member.id = :memberId
                AND (e.sentence LIKE %:keyword% OR e.description LIKE %:keyword%)
            """)
    List<ExpressionBookItem> findByMemberIdAndKeyword(@Param("memberId") Long memberId, @Param("keyword") String keyword);

    int countByIdExpressionBookId(Long id);

    int countByIdExpressionBookIdAndLearnedTrue(Long expressionBookId);
}
