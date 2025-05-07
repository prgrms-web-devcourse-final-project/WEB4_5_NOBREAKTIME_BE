package com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository;

import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpressionBookItemRepository extends JpaRepository<ExpressionBookItem, ExpressionBookItemId> {
    List<ExpressionBookItem> findAllById_ExpressionBookId(Long expressionBookId);

    boolean existsById(ExpressionBookItemId itemId);

	Optional<ExpressionBookItem> findById(ExpressionBookItemId itemId);

    void deleteAllById_ExpressionBookId(Long expressionBookId);
}
