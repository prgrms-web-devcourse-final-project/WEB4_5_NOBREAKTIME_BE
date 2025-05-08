package com.mallang.mallang_backend.domain.sentence.expressionbook.service;

import java.util.List;

import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.DeleteExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookResponse;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionResponse;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.MoveExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionSaveRequest;

import jakarta.validation.Valid;

public interface ExpressionBookService {
    ExpressionBookResponse create(@Valid ExpressionBookRequest request, Long memberId);

    List<ExpressionBookResponse> getByMember(Long memberId);

    void updateName(Long expressionBookId, Long memberId, String newName);

    void delete(Long expressionBookId, Long memberId);

    List<ExpressionResponse> getExpressionsByBook(Long expressionBookId, Long memberId);

    void save(ExpressionSaveRequest request, Long expressionBookId);

    void deleteExpressionsFromExpressionBook(DeleteExpressionsRequest request, Long memberId);

    void moveExpressions(MoveExpressionsRequest request, Long memberId);

    List<ExpressionResponse> searchExpressions(Long memberId, String keyword);
}
