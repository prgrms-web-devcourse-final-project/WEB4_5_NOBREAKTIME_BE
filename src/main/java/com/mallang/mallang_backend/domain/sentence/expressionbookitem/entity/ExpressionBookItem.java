package com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpressionBookItem {

    @EmbeddedId
    private ExpressionBookItemId id;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean learned = false;

    @Builder
    public ExpressionBookItem(
            Long expressionId,
            Long expressionBookId
    ) {
        this.id = new ExpressionBookItemId(expressionId, expressionBookId);
        this.createdAt = LocalDateTime.now();
    }

    public void updateLearned(boolean isLearned) {
        this.learned = isLearned;
    }
}