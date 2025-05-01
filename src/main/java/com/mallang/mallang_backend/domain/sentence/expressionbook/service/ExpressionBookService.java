package com.mallang.mallang_backend.domain.sentence.expressionbook.service;

import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookResponse;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface ExpressionBookService {
    ExpressionBookResponse create(@Valid ExpressionBookRequest request, Long memberId);

    List<ExpressionBookResponse> getByMember(Long memberId);

    void updateName(Long expressionBookId, Long memberId, String newName);

    void delete(Long expressionBookId, Long memberId);

    List<ExpressionResponse> getExpressionsByBook(Long expressionBookId, Long memberId);
}
