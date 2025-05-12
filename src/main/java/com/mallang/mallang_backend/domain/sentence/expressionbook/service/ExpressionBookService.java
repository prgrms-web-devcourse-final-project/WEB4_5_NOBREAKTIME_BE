package com.mallang.mallang_backend.domain.sentence.expressionbook.service;

import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.*;
import jakarta.validation.Valid;

import java.util.List;

public interface ExpressionBookService {
    ExpressionBookResponse create(@Valid ExpressionBookRequest request, Long memberId);

    List<ExpressionBookResponse> getByMember(Long memberId);

    void updateName(Long expressionBookId, Long memberId, String newName);

    void delete(Long expressionBookId, Long memberId);

    List<ExpressionResponse> getExpressionsByBook(List<Long> expressionBookIds, Long memberId);

    void save(ExpressionSaveRequest request, Long expressionBookId);

    void deleteExpressionsFromExpressionBook(DeleteExpressionsRequest request, Long memberId);

    void moveExpressions(MoveExpressionsRequest request, Long memberId);

    List<ExpressionResponse> searchExpressions(Long memberId, String keyword);
}
