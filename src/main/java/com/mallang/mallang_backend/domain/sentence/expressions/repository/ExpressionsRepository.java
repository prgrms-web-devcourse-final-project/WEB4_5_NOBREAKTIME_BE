package com.mallang.mallang_backend.domain.sentence.expressions.repository;

import com.mallang.mallang_backend.domain.sentence.expressions.entity.Expressions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpressionsRepository extends JpaRepository<Expressions, Long> {
}
