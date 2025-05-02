package com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItemId;

public interface ExpressionBookItemRepository extends JpaRepository<ExpressionBookItem, ExpressionBookItemId> {
    List<ExpressionBookItem> findAllById_ExpressionBookId(Long expressionBookId);

    boolean existsById(ExpressionBookItemId itemId);

	Optional<ExpressionBookItem> findById(ExpressionBookItemId itemId);
}
