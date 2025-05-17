package com.mallang.mallang_backend.domain.plan.entity.domain.sentence.expressionbook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionSaveRequest {
    private String videoId;  // 영상 아이디
    private Long subtitleId; // 자막 아이디
}
