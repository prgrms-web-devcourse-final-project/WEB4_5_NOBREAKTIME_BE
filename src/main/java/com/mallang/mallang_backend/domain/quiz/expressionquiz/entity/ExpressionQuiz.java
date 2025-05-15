package com.mallang.mallang_backend.domain.quiz.expressionquiz.entity;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.entity.BaseTime;
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
public class ExpressionQuiz extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expression_quiz_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(nullable = false)
    private Long learningTime = 0L; // 분, 초

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    // 생성 메서드
    @Builder
    public ExpressionQuiz(
        Member member,
        Language language
    ) {
        this.member = member;
        this.language = language;
    }

    /**
     * 퀴즈에서 학습한 시간을 추가합니다.
     * @param learningTime 퀴즈를 푸는데 걸린 총 시간
     */
    public void addLearningTime(Long learningTime) {
        if (learningTime != null && 0 < learningTime) {
            this.learningTime += learningTime;
        }
    }
}