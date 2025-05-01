package com.mallang.mallang_backend.domain.sentence.expressionbook.entity;

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
public class ExpressionBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ExpressionBook_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Builder
    public ExpressionBook(
            Member member,
            String name,
            Language language
    ) {
        this.member = member;
        this.name = name;
        this.language = language;
    }

    public void updateName(String newName) {
        this.name = newName;
    }
}
