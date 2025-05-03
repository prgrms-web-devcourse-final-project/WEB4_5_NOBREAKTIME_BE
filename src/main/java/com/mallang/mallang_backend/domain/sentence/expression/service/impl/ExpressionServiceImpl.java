package com.mallang.mallang_backend.domain.sentence.expression.service.impl;

import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.sentence.expression.service.ExpressionService;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpressionServiceImpl implements ExpressionService {

    private final ExpressionRepository expressionRepository;

    @Transactional
    @Override
    public List<ExpressionResponse> searchExpressions(String keyword) {
        List<Expression> expressions =
                expressionRepository.findBySentenceContainingIgnoreCase(keyword);

        return expressions.stream()
                .map(ExpressionResponse::from)
                .toList();
    }

}
