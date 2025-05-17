package com.mallang.mallang_backend.domain.plan.entity.domain.video.video.dto;

import com.mallang.mallang_backend.global.gpt.dto.GptSubtitleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AnalyzeVideoResponse {
    private List<GptSubtitleResponse> subtitleResults;

    public static AnalyzeVideoResponse from(List<GptSubtitleResponse> results) {
        return new AnalyzeVideoResponse(results);
    }
}
