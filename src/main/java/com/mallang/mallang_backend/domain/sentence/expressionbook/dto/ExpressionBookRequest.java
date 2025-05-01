package com.mallang.mallang_backend.domain.sentence.expressionbook.dto;

import com.mallang.mallang_backend.global.common.Language;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionBookRequest {
    private String name;
    private Language language;
}
