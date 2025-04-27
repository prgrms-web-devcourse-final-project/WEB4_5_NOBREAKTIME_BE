package com.mallang.mallang_backend.domain.sentence.expressionbook.repository;

import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpressionBookRepository extends JpaRepository<ExpressionBook, Long> {
}
