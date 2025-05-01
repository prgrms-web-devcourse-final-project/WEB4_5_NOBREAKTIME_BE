package com.mallang.mallang_backend.domain.sentence.expressionbookitem.service;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.AddExpressionToBookListRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.DeleteExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.MoveExpressionsRequest;

public interface ExpressionBookItemService {
    void addExpressionsFromVideo(Long expressionBookId, AddExpressionToBookListRequest request, Member member);

    void deleteExpressionsFromBook(DeleteExpressionsRequest request);

    void moveExpressions(MoveExpressionsRequest request);
}
