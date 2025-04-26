package com.mallang.mallang_backend.domain.sentence.expressionsItem.repository;

import com.mallang.mallang_backend.domain.sentence.expressionsItem.entity.ExpressionsItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpressionListRepository extends JpaRepository<ExpressionsItem, Long> {
}
