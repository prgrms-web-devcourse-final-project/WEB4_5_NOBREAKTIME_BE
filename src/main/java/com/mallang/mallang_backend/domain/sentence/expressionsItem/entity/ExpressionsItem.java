package com.mallang.mallang_backend.domain.sentence.expressionsItem.entity;

import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expressions.entity.Expressions;
import com.mallang.mallang_backend.domain.video.entity.Video;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpressionsItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expressions_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expressions_id", nullable = false)
    private Expressions expressions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expression_id", nullable = false)
    private Expression expression;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public ExpressionsItem(
            Expression expression,
            Expressions expressions,
            Video video
    ) {
        this.expression = expression;
        this.expressions = expressions;
        this.video = video;
    }
}