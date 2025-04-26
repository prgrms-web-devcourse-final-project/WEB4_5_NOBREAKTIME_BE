package com.mallang.mallang_backend.domain.sentence.expression.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expression {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expression_id")
    private Long id;

    @Column(nullable = false)
    private String sentence;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sentenceAnalysis;

    @Builder
    public Expression(
        String sentence,
        String description,
        String sentenceAnalysis
    ) {
        this.sentence = sentence;
        this.description = description;
        this.sentenceAnalysis = sentenceAnalysis;
    }
}