package com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItemId;

public interface ExpressionBookItemRepository extends JpaRepository<ExpressionBookItem, ExpressionBookItemId> {
    List<ExpressionBookItem> findAllById_ExpressionBookId(Long expressionBookId);

    boolean existsById(ExpressionBookItemId itemId);

	List<ExpressionBookItem> findAllByExpressionBook(ExpressionBook expressionBook);
}
