package com.mallang.mallang_backend.domain.sentence.expressionbook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddExpressionToBookRequest {
    private String sentence;
    private String description;
    private String sentenceAnalysis;
    private String videoId;
    private String subtitleAt;  // "00:01:23" 형식
}
