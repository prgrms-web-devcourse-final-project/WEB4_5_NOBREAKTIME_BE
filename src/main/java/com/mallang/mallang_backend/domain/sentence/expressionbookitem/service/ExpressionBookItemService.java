package com.mallang.mallang_backend.domain.sentence.expressionbookitem.service;

import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.DeleteExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.MoveExpressionsRequest;

public interface ExpressionBookItemService {
    void deleteExpressionsFromBook(DeleteExpressionsRequest request);

    void moveExpressions(MoveExpressionsRequest request);
}
