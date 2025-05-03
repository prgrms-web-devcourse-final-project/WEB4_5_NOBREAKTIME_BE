package com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

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