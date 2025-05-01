package com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository;

import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpressionBookItemRepository extends JpaRepository<ExpressionBookItem, ExpressionBookItemId> {
    List<ExpressionBookItem> findAllById_ExpressionBookId(Long expressionBookId);

    boolean existsById(ExpressionBookItemId itemId);
}
