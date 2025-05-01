package com.mallang.mallang_backend.domain.sentence.expressionbook.service;

import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.savedExpressionsRequest;

public interface ExpressionBookService {

    void save(savedExpressionsRequest request, Long expressionbookId);
}
