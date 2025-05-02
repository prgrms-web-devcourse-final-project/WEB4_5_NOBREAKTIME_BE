package com.mallang.mallang_backend.domain.sentence.expressionbook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteExpressionsRequest {
    private Long expressionBookId;
    private List<Long> expressionIds;
}
