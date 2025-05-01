package com.mallang.mallang_backend.domain.sentence.expressionbook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class savedExpressionsRequest {

    private String videoId;             // 영상 아이디
    private String sentence;          // 원문
    private String description;       // 원문 해석
    private LocalTime subtitleAt;    // 자막 시작 시점
    private LocalDateTime savedAt;    // 표현 저장일시
}
