package com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class ExpressionBookItemId implements Serializable {

    @Column(name = "expression_id")
    private Long expressionId;

    @Column(name = "expression_book_id")
    private Long expressionBookId;
}