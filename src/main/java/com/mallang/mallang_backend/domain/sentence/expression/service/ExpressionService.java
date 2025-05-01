package com.mallang.mallang_backend.domain.sentence.expression.service;

import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionResponse;

import java.util.List;

public interface ExpressionService {
    List<ExpressionResponse> searchExpressions(String keyword);
}
