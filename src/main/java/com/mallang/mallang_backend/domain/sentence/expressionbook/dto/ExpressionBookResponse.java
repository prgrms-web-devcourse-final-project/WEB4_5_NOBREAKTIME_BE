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
    private Long id;
    private String name;
    private Language language;
    private Long memberId;

    // 표현함 등록 응답을 위한 DTO 생성자
    public static ExpressionBookResponse from(ExpressionBook book) {
        return new ExpressionBookResponse(
                book.getId(),
                book.getName(),
                book.getLanguage(),
                book.getMember().getId()
        );
    }
}
