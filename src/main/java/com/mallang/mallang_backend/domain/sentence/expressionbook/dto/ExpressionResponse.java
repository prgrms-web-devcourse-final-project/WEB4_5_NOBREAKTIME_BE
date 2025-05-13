package com.mallang.mallang_backend.domain.sentence.expressionbook.dto;

import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
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
public class ExpressionResponse {
    private Long expressionId;
    private String sentence;
    private String description;
    private String sentenceAnalysis;
    private String thumbnailImageUrl;
    private String videoId;
    private String videoTitle;
    private LocalTime subtitleAt;
    private LocalDateTime createdAt;

    public static ExpressionResponse from(Expression expression, LocalDateTime createdAt) {
        return new ExpressionResponse(
                expression.getId(),
                expression.getSentence(),
                expression.getDescription(),
                expression.getSentenceAnalysis(),
                expression.getVideos() != null ? expression.getVideos().getThumbnailImageUrl() : null,
                expression.getVideos() != null ? expression.getVideos().getId() : null,
                expression.getVideos() != null ? expression.getVideos().getVideoTitle() : null,
                expression.getSubtitleAt(),
                createdAt
        );
    }
}