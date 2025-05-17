package com.mallang.mallang_backend.domain.plan.entity.domain.sentence.expressionbook.service;

import com.mallang.mallang_backend.domain.plan.entity.domain.sentence.expressionbook.dto.*;

import java.util.List;

public interface ExpressionBookService {
    Long create(ExpressionBookRequest request, Long memberId);

    List<ExpressionBookResponse> getByMember(Long memberId);

    void updateName(Long expressionBookId, Long memberId, String newName);

    void delete(Long expressionBookId, Long memberId);

    List<ExpressionResponse> getExpressionsByBook(Long memberId);

    void save(ExpressionSaveRequest request, Long expressionBookId, Long memberId);

    void deleteExpressionsFromExpressionBook(DeleteExpressionsRequest request, Long memberId);

    void moveExpressions(MoveExpressionsRequest request, Long memberId);

    List<ExpressionResponse> searchExpressions(Long memberId, String keyword);
}
