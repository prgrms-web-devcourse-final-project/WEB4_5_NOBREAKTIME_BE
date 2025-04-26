package com.mallang.mallang_backend.domain.sentence.expressions.entity;

import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expressions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expressions_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String expressionsName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Builder
    public Expressions(
            Member member,
            String expressionsName,
            Language language
    ) {
        this.member = member;
        this.expressionsName = expressionsName;
        this.language = language;
    }
}
