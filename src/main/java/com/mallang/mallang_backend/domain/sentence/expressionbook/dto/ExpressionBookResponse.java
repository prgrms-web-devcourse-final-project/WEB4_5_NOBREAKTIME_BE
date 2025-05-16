package com.mallang.mallang_backend.domain.sentence.expressionbook.dto;

import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.global.common.Language;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionBookResponse {
    private Long expressionBookId;
    private String name;
    private Language language;
    private int expressionCount;
    private int learnedExpressionCount;

    // 표현함 등록 응답을 위한 DTO 생성자
    public static ExpressionBookResponse from(ExpressionBook book, int expressionCount, int learnedExpressionCount) {
        return new ExpressionBookResponse(
                book.getId(),
                book.getName(),
                book.getLanguage(),
                expressionCount,
                learnedExpressionCount
        );
    }
}
