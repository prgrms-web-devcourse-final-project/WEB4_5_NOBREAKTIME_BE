package com.mallang.mallang_backend.domain.quiz.expressionquiz.entity;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.domain.quiz.wordquiz.entity.QuizType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * 표현 퀴즈
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpressionQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expression_quiz_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(nullable = false)
    private Integer learningTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    // 생성 메서드
    @Builder
    public ExpressionQuiz(
        Member member,
        Integer learningTime,
        Language language
    ) {
        this.member = member;
        this.learningTime = learningTime;
        this.language = language;
    }
}