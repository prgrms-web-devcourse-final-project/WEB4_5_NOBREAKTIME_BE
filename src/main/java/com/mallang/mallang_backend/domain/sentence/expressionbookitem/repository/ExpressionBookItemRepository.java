package com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository;

import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpressionBookItemRepository extends JpaRepository<ExpressionBookItem, Long> {
}
