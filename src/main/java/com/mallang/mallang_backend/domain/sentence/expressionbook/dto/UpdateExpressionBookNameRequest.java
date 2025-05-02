package com.mallang.mallang_backend.domain.sentence.expressionbook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExpressionBookNameRequest {
    private String newName;
}
