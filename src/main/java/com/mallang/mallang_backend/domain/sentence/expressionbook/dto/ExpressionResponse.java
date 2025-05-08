package com.mallang.mallang_backend.domain.sentence.expressionbook.dto;

import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionResponse {
    private Long expressionId;
    private String sentence;
    private String description;
    private String sentenceAnalysis;
    private String subtitleAt;

    public static ExpressionResponse from(Expression expression) {
        return new ExpressionResponse(
                expression.getId(),
                expression.getSentence(),
                expression.getDescription(),
                expression.getSentenceAnalysis(),
                expression.getSubtitleAt().toString()
        );
    }
}