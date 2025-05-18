package com.mallang.mallang_backend.domain.sentence.expressionbook.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoveExpressionsRequest {
    @NotNull(message = "원본 표현함 Id는 필수입니다.")
    private Long sourceExpressionBookId;

    @NotNull(message = "대상 표현함 Id는 필수입니다.")
    private Long targetExpressionBookId;

    @NotNull(message = "이동할 표현 Id 리스트는 필수입니다.")
    @Size(min = 1, message = "이동할 표현이 최소 1개 이상이어야 합니다.")
    private List<@NotNull(message = "표현 ID는 null일 수 없습니다.") Long> expressionIds;
}

