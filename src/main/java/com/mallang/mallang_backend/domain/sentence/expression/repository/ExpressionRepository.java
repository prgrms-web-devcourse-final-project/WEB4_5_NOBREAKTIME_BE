package com.mallang.mallang_backend.domain.sentence.expression.repository;

import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpressionRepository extends JpaRepository<Expression, Long> {
}
