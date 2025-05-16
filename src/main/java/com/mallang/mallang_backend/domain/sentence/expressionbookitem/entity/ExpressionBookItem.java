package com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity;

import com.mallang.mallang_backend.global.entity.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpressionBookItem extends BaseTime {

    @EmbeddedId
    private ExpressionBookItemId id;

    @Column(nullable = false)
    private boolean learned = false;

    @Builder
    public ExpressionBookItem(
            Long expressionId,
            Long expressionBookId
    ) {
        this.id = new ExpressionBookItemId(expressionId, expressionBookId);
    }

    public void updateLearned(boolean isLearned) {
        this.learned = isLearned;
    }
}